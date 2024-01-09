package ru.kcheranev.trading.core.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.port.income.trading.*
import ru.kcheranev.trading.core.port.outcome.broker.GetLastHistoricCandlesCommand
import ru.kcheranev.trading.core.port.outcome.broker.HistoricCandleBrokerPort
import ru.kcheranev.trading.core.port.outcome.persistence.*
import ru.kcheranev.trading.core.strategy.StrategyFactoryProvider
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.TradeDirection
import ru.kcheranev.trading.domain.entity.TradeOrder
import ru.kcheranev.trading.domain.entity.TradeSession

@Service
class TradeService(
    private val strategyConfigurationPersistencePort: StrategyConfigurationPersistencePort,
    private val tradeSessionPersistencePort: TradeSessionPersistencePort,
    private val tradeOrderPersistencePort: TradeOrderPersistencePort,
    private val historicCandleBrokerPort: HistoricCandleBrokerPort,
    private val strategyFactoryProvider: StrategyFactoryProvider
) : CreateStrategyConfigurationUseCase,
    StartTradeSessionUseCase,
    ReceiveCandleUseCase,
    EnterTradeSessionUseCase,
    ExitTradeSessionUseCase,
    StopTradeSessionUseCase {

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
    override fun startTradeSession(command: StartTradeSessionCommand) {
        val ticker = command.instrument.ticker
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
        val tradeSession = TradeSession.start(
            strategyConfiguration = strategyConfiguration,
            ticker = ticker,
            instrumentId = command.instrument.id,
            lotsQuantity = command.lotsQuantity,
            candles = candles,
            strategyFactory = strategyFactory
        )
        tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
    }

    @Transactional
    override fun processIncomeCandle(command: ProcessIncomeCandleCommand) {
        val candle = command.candle
        tradeSessionPersistencePort.getReadyToOrderTradeSessions(
            GetReadyToOrderTradeSessionsCommand(
                candle.instrumentId,
                candle.interval
            )
        ).forEach { tradeSession ->
            tradeSession.addBar(candle)
            if (tradeSession.shouldEnter()) {
                tradeSession.pendingEnter()
                tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
            } else if (tradeSession.shouldExit()) {
                tradeSession.pendingExit()
                tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
            }
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
                    price = command.totalPrice,
                    direction = TradeDirection.BUY,
                    tradeSessionId = id!!
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
                    price = command.totalPrice,
                    direction = TradeDirection.SELL,
                    tradeSessionId = id!!
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