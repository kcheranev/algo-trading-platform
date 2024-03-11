package ru.kcheranev.trading.core.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import ru.kcheranev.trading.common.DateSupplier
import ru.kcheranev.trading.core.config.TradingProperties
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
import ru.kcheranev.trading.core.port.outcome.broker.GetHistoricCandlesCommand
import ru.kcheranev.trading.core.port.outcome.broker.GetLastHistoricCandlesCommand
import ru.kcheranev.trading.core.port.outcome.broker.HistoricCandleBrokerPort
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
import ru.kcheranev.trading.domain.model.Candle

@Service
class TradeService(
    tradingProperties: TradingProperties,
    private val transactionTemplate: TransactionTemplate,
    private val strategyConfigurationPersistencePort: StrategyConfigurationPersistencePort,
    private val tradeSessionPersistencePort: TradeSessionPersistencePort,
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

    private val candleDelaysProperties = tradingProperties.candleDelaysProperties

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
        ).forEach { tradeSession -> addCandleToTradeSessionStrategySeries(tradeSession, candle) }
    }

    private fun addCandleToTradeSessionStrategySeries(tradeSession: TradeSession, candle: Candle) {
        try {
            transactionTemplate.executeWithoutResult {
                if (tradeSession.expiredCandleSeries(candleDelaysProperties.maxAvailableCount.toLong(), dateSupplier)) {
                    tradeSession.expire()
                    tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
                    return@executeWithoutResult
                }
                if (tradeSession.freshCandleSeries(candleDelaysProperties.availableCount.toLong(), dateSupplier)) {
                    tradeSession.addCandle(candle, candleDelaysProperties.availableCount.toLong())
                } else {
                    val candles =
                        historicCandleBrokerPort.getHistoricCandles(
                            GetHistoricCandlesCommand(
                                tradeSession.instrument,
                                tradeSession.candleInterval,
                                tradeSession.lastCandleDate().plus(tradeSession.candleIntervalDuration),
                                dateSupplier.currentDate()
                            )
                        )
                    tradeSession.refreshCandleSeries(candles)
                }
                tradeSession.executeStrategy()
                tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
            }
        } catch (ex: Exception) {
            log.warn("An error has been occurred while processing income candle", ex)
        }
    }

    @Transactional
    override fun enterTradeSession(command: EnterTradeSessionCommand) {
        val tradeSession = tradeSessionPersistencePort.get(GetTradeSessionCommand(command.tradeSessionId))
        tradeSession.enter()
        val tradeOrder =
            with(tradeSession) {
                TradeOrder.create(
                    ticker = ticker,
                    instrumentId = instrumentId,
                    lotsQuantity = lotsQuantity,
                    totalPrice = command.totalPrice,
                    executedCommission = command.executedCommission,
                    direction = TradeDirection.BUY,
                    tradeSessionId = id!!,
                    dateSupplier = dateSupplier
                )
            }
        tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
        tradeOrderPersistencePort.save(SaveOrderCommand(tradeOrder))
    }

    @Transactional
    override fun exitTradeSession(command: ExitTradeSessionCommand) {
        val tradeSession = tradeSessionPersistencePort.get(GetTradeSessionCommand(command.tradeSessionId))
        tradeSession.exit()
        val tradeOrder =
            with(tradeSession) {
                TradeOrder.create(
                    ticker = ticker,
                    instrumentId = instrumentId,
                    lotsQuantity = lotsQuantity,
                    totalPrice = command.totalPrice,
                    executedCommission = command.executedCommission,
                    direction = TradeDirection.SELL,
                    tradeSessionId = id!!,
                    dateSupplier = dateSupplier
                )
            }
        tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
        tradeOrderPersistencePort.save(SaveOrderCommand(tradeOrder))
    }

    @Transactional
    override fun stopTradeSession(command: StopTradeSessionCommand) {
        val tradeSession = tradeSessionPersistencePort.get(GetTradeSessionCommand(command.tradeSessionId))
        tradeSession.stop()
        tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
    }

}