package ru.kcheranev.trading.core.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.common.date.DateSupplier
import ru.kcheranev.trading.core.config.TradingProperties
import ru.kcheranev.trading.core.model.order.PostOrderResultAccumulator
import ru.kcheranev.trading.core.port.income.tradesession.EnterTradeSessionCommand
import ru.kcheranev.trading.core.port.income.tradesession.EnterTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.tradesession.ExitTradeSessionCommand
import ru.kcheranev.trading.core.port.income.tradesession.ExitTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.tradesession.SearchTradeSessionCommand
import ru.kcheranev.trading.core.port.income.tradesession.SearchTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.tradesession.StartTradeSessionCommand
import ru.kcheranev.trading.core.port.income.tradesession.StartTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.tradesession.StopTradeSessionCommand
import ru.kcheranev.trading.core.port.income.tradesession.StopTradeSessionUseCase
import ru.kcheranev.trading.core.port.mapper.commandMapper
import ru.kcheranev.trading.core.port.outcome.broker.GetLastHistoricCandlesCommand
import ru.kcheranev.trading.core.port.outcome.broker.HistoricCandleBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.OrderServiceBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.PostBestPriceBuyOrderCommand
import ru.kcheranev.trading.core.port.outcome.broker.PostBestPriceSellOrderCommand
import ru.kcheranev.trading.core.port.outcome.broker.model.PostOrderResponse
import ru.kcheranev.trading.core.port.outcome.persistence.strategyconfiguration.GetStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.outcome.persistence.strategyconfiguration.StrategyConfigurationPersistencePort
import ru.kcheranev.trading.core.port.outcome.persistence.tradeorder.SaveOrderCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradeorder.TradeOrderPersistencePort
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.GetTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.SaveTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.TradeSessionPersistencePort
import ru.kcheranev.trading.core.strategy.factory.StrategyFactoryProvider
import ru.kcheranev.trading.domain.entity.TradeOrder
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.model.TradeDirection

@Service
class TradeSessionService(
    tradingProperties: TradingProperties,
    private val strategyConfigurationPersistencePort: StrategyConfigurationPersistencePort,
    private val tradeSessionPersistencePort: TradeSessionPersistencePort,
    private val orderServiceBrokerPort: OrderServiceBrokerPort,
    private val tradeOrderPersistencePort: TradeOrderPersistencePort,
    private val historicCandleBrokerPort: HistoricCandleBrokerPort,
    private val strategyFactoryProvider: StrategyFactoryProvider,
    private val dateSupplier: DateSupplier
) : SearchTradeSessionUseCase,
    StartTradeSessionUseCase,
    EnterTradeSessionUseCase,
    ExitTradeSessionUseCase,
    StopTradeSessionUseCase {

    private val placeOrderRetryCount = tradingProperties.placeOrderRetryCount

    @Transactional
    override fun startTradeSession(command: StartTradeSessionCommand): TradeSessionId {
        val strategyConfiguration =
            strategyConfigurationPersistencePort.get(
                GetStrategyConfigurationCommand(command.strategyConfigurationId)
            )
        val strategyFactory = strategyFactoryProvider.getStrategyFactory(strategyConfiguration.type)
        val tradeSession =
            TradeSession.start(
                strategyConfiguration = strategyConfiguration,
                ticker = command.instrument.ticker,
                instrumentId = command.instrument.id,
                lotsQuantity = command.lotsQuantity,
                strategyFactory = strategyFactory,
                dateSupplier = dateSupplier
            )
        val initCandlesAmount = tradeSession.strategy.unstableBars
        if (initCandlesAmount > 0) {
            val candles =
                historicCandleBrokerPort.getLastHistoricCandles(
                    GetLastHistoricCandlesCommand(
                        command.instrument,
                        strategyConfiguration.candleInterval,
                        initCandlesAmount
                    )
                )
            tradeSession.initStrategySeries(candles)
        }
        return tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
    }

    @Transactional
    override fun enterTradeSession(command: EnterTradeSessionCommand) {
        val tradeSession = tradeSessionPersistencePort.get(GetTradeSessionCommand(command.tradeSessionId))
        val postOrderResultAccumulator =
            postOrderWithRetry(tradeSession.lotsQuantity) { remainLotsQuantity ->
                val postOrderResponse =
                    if (tradeSession.margin()) {
                        orderServiceBrokerPort.postBestPriceSellOrder(
                            PostBestPriceSellOrderCommand(tradeSession.instrument, remainLotsQuantity)
                        )
                    } else {
                        orderServiceBrokerPort.postBestPriceBuyOrder(
                            PostBestPriceBuyOrderCommand(tradeSession.instrument, remainLotsQuantity)
                        )
                    }
                if (postOrderResponse.executed()) {
                    val tradeOrder =
                        TradeOrder.create(
                            ticker = tradeSession.ticker,
                            instrumentId = tradeSession.instrumentId,
                            lotsQuantity = postOrderResponse.lotsExecuted,
                            totalPrice = postOrderResponse.totalPrice,
                            executedCommission = postOrderResponse.executedCommission,
                            direction = if (tradeSession.margin()) TradeDirection.SELL else TradeDirection.BUY,
                            tradeSessionId = tradeSession.id!!,
                            dateSupplier = dateSupplier
                        )
                    tradeOrderPersistencePort.save(SaveOrderCommand(tradeOrder))
                }
                return@postOrderWithRetry postOrderResponse
            }
        if (postOrderResultAccumulator.haveOrders()) {
            tradeSession.enter(postOrderResultAccumulator.lotsExecuted)
        } else {
            tradeSession.waitForEntry()
        }
        tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
    }

    @Transactional
    override fun exitTradeSession(command: ExitTradeSessionCommand) {
        val tradeSession = tradeSessionPersistencePort.get(GetTradeSessionCommand(command.tradeSessionId))
        val postOrderResultAccumulator =
            postOrderWithRetry(tradeSession.lotsQuantityInPosition) { remainLotsQuantity ->
                val postOrderResponse =
                    if (tradeSession.margin()) {
                        orderServiceBrokerPort.postBestPriceBuyOrder(
                            PostBestPriceBuyOrderCommand(tradeSession.instrument, remainLotsQuantity)
                        )
                    } else {
                        orderServiceBrokerPort.postBestPriceSellOrder(
                            PostBestPriceSellOrderCommand(tradeSession.instrument, remainLotsQuantity)
                        )
                    }
                if (postOrderResponse.executed()) {
                    val tradeOrder =
                        TradeOrder.create(
                            ticker = tradeSession.ticker,
                            instrumentId = tradeSession.instrumentId,
                            lotsQuantity = postOrderResponse.lotsExecuted,
                            totalPrice = postOrderResponse.totalPrice,
                            executedCommission = postOrderResponse.executedCommission,
                            direction = if (tradeSession.margin()) TradeDirection.BUY else TradeDirection.SELL,
                            tradeSessionId = tradeSession.id!!,
                            dateSupplier = dateSupplier
                        )
                    tradeOrderPersistencePort.save(SaveOrderCommand(tradeOrder))
                }
                return@postOrderWithRetry postOrderResponse
            }
        if (postOrderResultAccumulator.haveOrders()) {
            tradeSession.exit(postOrderResultAccumulator.lotsExecuted)
        } else {
            tradeSession.waitForEntry()
        }
        tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
    }

    private fun postOrderWithRetry(
        lotsQuantity: Int,
        postOrder: (requestedLotsQuantity: Int) -> PostOrderResponse
    ): PostOrderResultAccumulator {
        val postOrderResultAccumulator = PostOrderResultAccumulator(lotsQuantity)
        for (placeOrderTry in 1..placeOrderRetryCount) {
            val postOrderResponse = postOrder.invoke(postOrderResultAccumulator.remainLotsQuantity())
            postOrderResultAccumulator.accumulate(postOrderResponse)
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

    override fun search(command: SearchTradeSessionCommand) =
        tradeSessionPersistencePort.search(commandMapper.map(command))

}