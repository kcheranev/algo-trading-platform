package com.github.trading.core.service

import arrow.core.Either
import arrow.core.getOrElse
import com.github.trading.core.config.TradingProperties.Companion.tradingProperties
import com.github.trading.core.error.AppError
import com.github.trading.core.model.order.PostOrderResultAccumulator
import com.github.trading.core.port.income.tradesession.CreateTradeSessionCommand
import com.github.trading.core.port.income.tradesession.CreateTradeSessionUseCase
import com.github.trading.core.port.income.tradesession.EnterTradeSessionCommand
import com.github.trading.core.port.income.tradesession.EnterTradeSessionUseCase
import com.github.trading.core.port.income.tradesession.ExitTradeSessionCommand
import com.github.trading.core.port.income.tradesession.ExitTradeSessionUseCase
import com.github.trading.core.port.income.tradesession.ResumeTradeSessionCommand
import com.github.trading.core.port.income.tradesession.ResumeTradeSessionUseCase
import com.github.trading.core.port.income.tradesession.SearchTradeSessionCommand
import com.github.trading.core.port.income.tradesession.SearchTradeSessionUseCase
import com.github.trading.core.port.income.tradesession.StopTradeSessionCommand
import com.github.trading.core.port.income.tradesession.StopTradeSessionUseCase
import com.github.trading.core.port.mapper.commandMapper
import com.github.trading.core.port.outcome.broker.OrderServiceBrokerPort
import com.github.trading.core.port.outcome.broker.PostBestPriceBuyOrderCommand
import com.github.trading.core.port.outcome.broker.PostBestPriceSellOrderCommand
import com.github.trading.core.port.outcome.broker.model.PostOrderResponse
import com.github.trading.core.port.outcome.persistence.strategyconfiguration.GetStrategyConfigurationCommand
import com.github.trading.core.port.outcome.persistence.strategyconfiguration.StrategyConfigurationPersistencePort
import com.github.trading.core.port.outcome.persistence.tradeorder.InsertTradeOrderCommand
import com.github.trading.core.port.outcome.persistence.tradeorder.TradeOrderPersistencePort
import com.github.trading.core.port.outcome.persistence.tradesession.GetTradeSessionCommand
import com.github.trading.core.port.outcome.persistence.tradesession.InsertTradeSessionCommand
import com.github.trading.core.port.outcome.persistence.tradesession.SaveTradeSessionCommand
import com.github.trading.core.port.outcome.persistence.tradesession.TradeSessionPersistencePort
import com.github.trading.core.port.service.TradeStrategyServicePort
import com.github.trading.core.port.service.command.InitTradeStrategyCommand
import com.github.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyProvider
import com.github.trading.domain.entity.TradeOrder
import com.github.trading.domain.entity.TradeSession
import com.github.trading.domain.entity.TradeSessionId
import com.github.trading.domain.model.TradeDirection
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Service
class TradeSessionService(
    private val tradeStrategyServicePort: TradeStrategyServicePort,
    private val strategyConfigurationPersistencePort: StrategyConfigurationPersistencePort,
    private val tradeSessionPersistencePort: TradeSessionPersistencePort,
    private val orderServiceBrokerPort: OrderServiceBrokerPort,
    private val tradeOrderPersistencePort: TradeOrderPersistencePort,
    private val orderLotsQuantityStrategyProvider: OrderLotsQuantityStrategyProvider
) : SearchTradeSessionUseCase,
    CreateTradeSessionUseCase,
    EnterTradeSessionUseCase,
    ExitTradeSessionUseCase,
    StopTradeSessionUseCase,
    ResumeTradeSessionUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    private val orderLock = ReentrantLock()

    @Transactional
    override fun createTradeSession(command: CreateTradeSessionCommand): TradeSessionId {
        val strategyConfiguration =
            strategyConfigurationPersistencePort.get(
                GetStrategyConfigurationCommand(command.strategyConfigurationId)
            )
        val tradeStrategy =
            tradeStrategyServicePort.initTradeStrategy(
                InitTradeStrategyCommand(
                    strategyType = strategyConfiguration.type,
                    instrument = command.instrument,
                    candleInterval = strategyConfiguration.candleInterval,
                    strategyParameters = strategyConfiguration.parameters
                )
            )
        val tradeSession =
            TradeSession.create(
                strategyConfiguration = strategyConfiguration,
                ticker = command.instrument.ticker,
                instrumentId = command.instrument.id,
                orderLotsQuantityStrategy = orderLotsQuantityStrategyProvider.getOrderLotsQuantityStrategy(command.orderLotsQuantityStrategyType),
                tradeStrategy = tradeStrategy
            )
        tradeSessionPersistencePort.insert(InsertTradeSessionCommand(tradeSession))
        return tradeSession.id
    }

    @Transactional
    override fun enterTradeSession(command: EnterTradeSessionCommand) {
        orderLock.withLock {
            val tradeSession = tradeSessionPersistencePort.get(GetTradeSessionCommand(command.tradeSessionId))
            val lotsRequested =
                tradeSession.calculateOrderLotsQuantity()
                    .getOrElse { error ->
                        log.warn(error.message)
                        tradeSession.resume()
                        tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
                        return
                    }
            val postOrderResultAccumulator =
                postOrderWithRetry(lotsRequested) { remainLotsQuantity ->
                    if (tradeSession.isMargin()) {
                        orderServiceBrokerPort.postBestPriceSellOrder(
                            PostBestPriceSellOrderCommand(tradeSession.instrument, remainLotsQuantity)
                        )
                    } else {
                        orderServiceBrokerPort.postBestPriceBuyOrder(
                            PostBestPriceBuyOrderCommand(tradeSession.instrument, remainLotsQuantity)
                        )
                    }.onRight { postOrderResponse ->
                        if (postOrderResponse.executed()) {
                            val tradeOrder =
                                TradeOrder.create(
                                    ticker = tradeSession.ticker,
                                    instrumentId = tradeSession.instrumentId,
                                    lotsQuantity = postOrderResponse.lotsExecuted,
                                    totalPrice = postOrderResponse.totalPrice,
                                    executedCommission = postOrderResponse.executedCommission,
                                    direction = if (tradeSession.isMargin()) TradeDirection.SELL else TradeDirection.BUY,
                                    tradeSessionId = tradeSession.id
                                )
                            tradeOrderPersistencePort.insert(InsertTradeOrderCommand(tradeOrder))
                        }
                    }
                }

            if (postOrderResultAccumulator.haveOrders()) {
                tradeSession.enter(
                    lotsRequested,
                    postOrderResultAccumulator.lotsExecuted,
                    postOrderResultAccumulator.averagePrice
                )
            } else {
                tradeSession.resume()
            }
            tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
        }
    }

    @Transactional
    override fun exitTradeSession(command: ExitTradeSessionCommand) {
        val tradeSession = tradeSessionPersistencePort.get(GetTradeSessionCommand(command.tradeSessionId))
        val postOrderResultAccumulator =
            postOrderWithRetry(tradeSession.currentPosition.lotsQuantity) { remainLotsQuantity ->
                if (tradeSession.isMargin()) {
                    orderServiceBrokerPort.postBestPriceBuyOrder(
                        PostBestPriceBuyOrderCommand(tradeSession.instrument, remainLotsQuantity)
                    )
                } else {
                    orderServiceBrokerPort.postBestPriceSellOrder(
                        PostBestPriceSellOrderCommand(tradeSession.instrument, remainLotsQuantity)
                    )
                }.onRight { postOrderResponse ->
                    if (postOrderResponse.executed()) {
                        val tradeOrder =
                            TradeOrder.create(
                                ticker = tradeSession.ticker,
                                instrumentId = tradeSession.instrumentId,
                                lotsQuantity = postOrderResponse.lotsExecuted,
                                totalPrice = postOrderResponse.totalPrice,
                                executedCommission = postOrderResponse.executedCommission,
                                direction = if (tradeSession.isMargin()) TradeDirection.BUY else TradeDirection.SELL,
                                tradeSessionId = tradeSession.id
                            )
                        tradeOrderPersistencePort.insert(InsertTradeOrderCommand(tradeOrder))
                    }
                }
            }
        if (postOrderResultAccumulator.haveOrders()) {
            tradeSession.exit(postOrderResultAccumulator.lotsExecuted)
        } else {
            tradeSession.resume()
        }
        tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
    }

    private fun postOrderWithRetry(
        lotsQuantity: Int,
        postOrder: (Int) -> Either<AppError, PostOrderResponse>
    ): PostOrderResultAccumulator {
        val postOrderResultAccumulator = PostOrderResultAccumulator(lotsQuantity)
        for (placeOrderTry in 1..tradingProperties.placeOrderRetryCount) {
            postOrder(postOrderResultAccumulator.remainLotsQuantity())
                .onRight(postOrderResultAccumulator::accumulate)
            if (postOrderResultAccumulator.completed()) {
                break
            }
        }
        return postOrderResultAccumulator
    }

    @Transactional
    override fun stopTradeSession(command: StopTradeSessionCommand) {
        val tradeSession = tradeSessionPersistencePort.get(GetTradeSessionCommand(command.tradeSessionId))
        tradeSession.stop()
        tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
    }

    @Transactional
    override fun resumeTradeSession(command: ResumeTradeSessionCommand) {
        val tradeSession = tradeSessionPersistencePort.get(GetTradeSessionCommand(command.tradeSessionId))
        tradeSession.resume()
        tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
    }

    override fun search(command: SearchTradeSessionCommand) =
        tradeSessionPersistencePort.search(commandMapper.map(command))

}