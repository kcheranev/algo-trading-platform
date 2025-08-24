package ru.kcheranev.trading.core.service

import arrow.core.Either
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.config.TradingProperties.Companion.tradingProperties
import ru.kcheranev.trading.core.error.Error
import ru.kcheranev.trading.core.model.order.PostOrderResultAccumulator
import ru.kcheranev.trading.core.port.income.tradesession.CreateTradeSessionCommand
import ru.kcheranev.trading.core.port.income.tradesession.CreateTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.tradesession.EnterTradeSessionCommand
import ru.kcheranev.trading.core.port.income.tradesession.EnterTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.tradesession.ExitTradeSessionCommand
import ru.kcheranev.trading.core.port.income.tradesession.ExitTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.tradesession.ResumeTradeSessionCommand
import ru.kcheranev.trading.core.port.income.tradesession.ResumeTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.tradesession.SearchTradeSessionCommand
import ru.kcheranev.trading.core.port.income.tradesession.SearchTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.tradesession.StopTradeSessionCommand
import ru.kcheranev.trading.core.port.income.tradesession.StopTradeSessionUseCase
import ru.kcheranev.trading.core.port.mapper.commandMapper
import ru.kcheranev.trading.core.port.outcome.broker.OrderServiceBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.PostBestPriceBuyOrderCommand
import ru.kcheranev.trading.core.port.outcome.broker.PostBestPriceSellOrderCommand
import ru.kcheranev.trading.core.port.outcome.broker.WithdrawLimitsBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.model.PostOrderResponse
import ru.kcheranev.trading.core.port.outcome.persistence.strategyconfiguration.GetStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.outcome.persistence.strategyconfiguration.StrategyConfigurationPersistencePort
import ru.kcheranev.trading.core.port.outcome.persistence.tradeorder.InsertTradeOrderCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradeorder.TradeOrderPersistencePort
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.GetTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.InsertTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.SaveTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.TradeSessionPersistencePort
import ru.kcheranev.trading.core.port.service.TradeStrategyServicePort
import ru.kcheranev.trading.core.port.service.command.InitTradeStrategyCommand
import ru.kcheranev.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyProvider
import ru.kcheranev.trading.domain.entity.TradeOrder
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.model.TradeDirection
import java.math.BigDecimal

@Service
class TradeSessionService(
    private val tradeStrategyServicePort: TradeStrategyServicePort,
    private val strategyConfigurationPersistencePort: StrategyConfigurationPersistencePort,
    private val tradeSessionPersistencePort: TradeSessionPersistencePort,
    private val orderServiceBrokerPort: OrderServiceBrokerPort,
    private val tradeOrderPersistencePort: TradeOrderPersistencePort,
    private val withdrawLimitsBrokerPort: WithdrawLimitsBrokerPort,
    private val orderLotsQuantityStrategyProvider: OrderLotsQuantityStrategyProvider
) : SearchTradeSessionUseCase,
    CreateTradeSessionUseCase,
    EnterTradeSessionUseCase,
    ExitTradeSessionUseCase,
    StopTradeSessionUseCase,
    ResumeTradeSessionUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

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
        val tradeSession = tradeSessionPersistencePort.get(GetTradeSessionCommand(command.tradeSessionId))
        val lotsRequested = tradeSession.calculateOrderLotsQuantity()
        withdrawLimitsBrokerPort.getWithdrawLimits()
            .onRight { depositValue ->
                val expectedPrice =
                    tradeSession.lastCandleClosePrice() * lotsRequested.toBigDecimal() * BigDecimal(1.01)
                if (depositValue < expectedPrice) {
                    log.warn("It's unable to post order, not enough money on deposit")
                    tradeSession.resume()
                    tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
                    return
                }
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
        postOrder: (Int) -> Either<Error, PostOrderResponse>
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