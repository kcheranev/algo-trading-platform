package ru.kcheranev.trading.core.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import ru.kcheranev.trading.common.DateSupplier
import ru.kcheranev.trading.core.config.TradingProperties
import ru.kcheranev.trading.core.model.order.PostOrderResultAccumulator
import ru.kcheranev.trading.core.port.income.trading.CreateStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.income.trading.CreateStrategyConfigurationUseCase
import ru.kcheranev.trading.core.port.income.trading.EnterTradeSessionCommand
import ru.kcheranev.trading.core.port.income.trading.EnterTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.trading.ExitTradeSessionCommand
import ru.kcheranev.trading.core.port.income.trading.ExitTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.trading.ProcessIncomeCandleCommand
import ru.kcheranev.trading.core.port.income.trading.ReceiveCandleUseCase
import ru.kcheranev.trading.core.port.income.trading.StartTradeSessionCommand
import ru.kcheranev.trading.core.port.income.trading.StartTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.trading.StopTradeSessionCommand
import ru.kcheranev.trading.core.port.income.trading.StopTradeSessionUseCase
import ru.kcheranev.trading.core.port.outcome.broker.GetLastHistoricCandlesCommand
import ru.kcheranev.trading.core.port.outcome.broker.HistoricCandleBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.OrderServiceBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.PostBestPriceBuyOrderCommand
import ru.kcheranev.trading.core.port.outcome.broker.PostBestPriceSellOrderCommand
import ru.kcheranev.trading.core.port.outcome.broker.model.PostOrderResponse
import ru.kcheranev.trading.core.port.outcome.persistence.GetReadyToOrderTradeSessionsCommand
import ru.kcheranev.trading.core.port.outcome.persistence.GetStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.outcome.persistence.GetTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.SaveOrderCommand
import ru.kcheranev.trading.core.port.outcome.persistence.SaveStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.outcome.persistence.SaveTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.StrategyConfigurationPersistencePort
import ru.kcheranev.trading.core.port.outcome.persistence.TradeOrderPersistencePort
import ru.kcheranev.trading.core.port.outcome.persistence.TradeSessionPersistencePort
import ru.kcheranev.trading.core.strategy.StrategyFactoryProvider
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.TradeDirection
import ru.kcheranev.trading.domain.entity.TradeOrder
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId

@Service
class TradeService(
    tradingProperties: TradingProperties,
    private val transactionalTemplate: TransactionTemplate,
    private val strategyConfigurationPersistencePort: StrategyConfigurationPersistencePort,
    private val tradeSessionPersistencePort: TradeSessionPersistencePort,
    private val orderServiceBrokerPort: OrderServiceBrokerPort,
    private val tradeOrderPersistencePort: TradeOrderPersistencePort,
    private val historicCandleBrokerPort: HistoricCandleBrokerPort,
    private val strategyFactoryProvider: StrategyFactoryProvider,
    private val dateSupplier: DateSupplier
) : CreateStrategyConfigurationUseCase,
    StartTradeSessionUseCase,
    ReceiveCandleUseCase,
    EnterTradeSessionUseCase,
    ExitTradeSessionUseCase,
    StopTradeSessionUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    private val availableDelayedCandleCount = tradingProperties.availableDelayedCandleCount

    private val placeOrderRetryCount = tradingProperties.placeOrderRetryCount

    @Transactional
    override fun createStrategyConfiguration(command: CreateStrategyConfigurationCommand) {
        val strategyConfiguration =
            StrategyConfiguration.create(
                type = command.type,
                initCandleAmount = command.initCandleAmount,
                candleInterval = command.candleInterval,
                params = command.params
            )
        strategyConfigurationPersistencePort.save(SaveStrategyConfigurationCommand(strategyConfiguration))
    }

    @Transactional
    override fun startTradeSession(command: StartTradeSessionCommand): TradeSessionId {
        val strategyConfiguration =
            strategyConfigurationPersistencePort.get(
                GetStrategyConfigurationCommand(command.strategyConfigurationId)
            )
        val strategyFactory = strategyFactoryProvider.getStrategyFactory(strategyConfiguration.type)
        val candles =
            historicCandleBrokerPort.getLastHistoricCandles(
                GetLastHistoricCandlesCommand(
                    command.instrument,
                    strategyConfiguration.candleInterval,
                    strategyConfiguration.initCandleAmount
                )
            )
        val tradeSession =
            TradeSession.start(
                strategyConfiguration = strategyConfiguration,
                ticker = command.instrument.ticker,
                instrumentId = command.instrument.id,
                lotsQuantity = command.lotsQuantity,
                candles = candles,
                strategyFactory = strategyFactory,
                dateSupplier = dateSupplier
            )
        return tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
    }

    override fun processIncomeCandle(command: ProcessIncomeCandleCommand) {
        val candle = command.candle
        tradeSessionPersistencePort.getReadyToOrderTradeSessions(
            GetReadyToOrderTradeSessionsCommand(candle.instrumentId, candle.interval)
        ).forEach { tradeSession ->
            try {
                transactionalTemplate.execute {
                    tradeSession.processIncomeCandle(candle, availableDelayedCandleCount.toLong())
                    tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
                }
            } catch (ex: Exception) {
                log.warn("An error has been occurred while processing income candle", ex)
            }
        }
    }

    @Transactional
    override fun enterTradeSession(command: EnterTradeSessionCommand) {
        val tradeSession = tradeSessionPersistencePort.get(GetTradeSessionCommand(command.tradeSessionId))
        val postOrderResultAccumulator =
            postOrderWithRetry(tradeSession.lotsQuantity) { remainLotsQuantity ->
                val postOrderResponse =
                    orderServiceBrokerPort.postBestPriceBuyOrder(
                        PostBestPriceBuyOrderCommand(tradeSession.instrument, remainLotsQuantity)
                    )
                if (postOrderResponse.executed()) {
                    val tradeOrder =
                        TradeOrder.create(
                            ticker = tradeSession.ticker,
                            instrumentId = tradeSession.instrumentId,
                            lotsQuantity = postOrderResponse.lotsExecuted,
                            totalPrice = postOrderResponse.totalPrice,
                            executedCommission = postOrderResponse.executedCommission,
                            direction = TradeDirection.BUY,
                            strategyConfigurationId = tradeSession.strategyConfigurationId,
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
                    orderServiceBrokerPort.postBestPriceSellOrder(
                        PostBestPriceSellOrderCommand(tradeSession.instrument, remainLotsQuantity)
                    )
                if (postOrderResponse.executed()) {
                    val tradeOrder =
                        TradeOrder.create(
                            ticker = tradeSession.ticker,
                            instrumentId = tradeSession.instrumentId,
                            lotsQuantity = postOrderResponse.lotsExecuted,
                            totalPrice = postOrderResponse.totalPrice,
                            executedCommission = postOrderResponse.executedCommission,
                            direction = TradeDirection.SELL,
                            strategyConfigurationId = tradeSession.strategyConfigurationId,
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

}