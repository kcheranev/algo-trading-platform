package ru.kcheranev.trading.core.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.port.income.trading.EnterTradeSessionCommand
import ru.kcheranev.trading.core.port.income.trading.EnterTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.trading.ExitTradeSessionCommand
import ru.kcheranev.trading.core.port.income.trading.ExitTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.trading.ProcessIncomeCandleCommand
import ru.kcheranev.trading.core.port.income.trading.ReceiveCandleUseCase
import ru.kcheranev.trading.core.port.income.trading.StartTradeSessionCommand
import ru.kcheranev.trading.core.port.income.trading.StartTradeSessionUseCase
import ru.kcheranev.trading.core.port.outcome.broker.GetLastHistoricCandlesCommand
import ru.kcheranev.trading.core.port.outcome.broker.HistoricCandleBrokerPort
import ru.kcheranev.trading.core.port.outcome.persistence.GetReadyToOrderTradeSessionsCommand
import ru.kcheranev.trading.core.port.outcome.persistence.GetStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.outcome.persistence.GetTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.SaveTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.StrategyConfigurationPersistencePort
import ru.kcheranev.trading.core.port.outcome.persistence.TradeSessionPersistencePort
import ru.kcheranev.trading.core.strategy.StrategyFactoryProvider
import ru.kcheranev.trading.domain.entity.TradeSession

@Service
class TradeService(
    private val strategyConfigurationPersistencePort: StrategyConfigurationPersistencePort,
    private val tradeSessionPersistencePort: TradeSessionPersistencePort,
    private val historicCandleBrokerPort: HistoricCandleBrokerPort,
    private val strategyFactoryProvider: StrategyFactoryProvider
) : StartTradeSessionUseCase,
    ReceiveCandleUseCase,
    EnterTradeSessionUseCase,
    ExitTradeSessionUseCase {

    @Transactional
    override fun startTradeSession(command: StartTradeSessionCommand) {
        val ticker = command.instrument.ticker
        val strategyConfiguration =
            strategyConfigurationPersistencePort.get(
                GetStrategyConfigurationCommand(command.strategyConfigurationId)
            )
        val strategyFactory = strategyFactoryProvider.getStrategyFactory(command.strategyType)
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
        tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
    }

    @Transactional
    override fun exitTradeSession(command: ExitTradeSessionCommand) {
        val tradeSession = tradeSessionPersistencePort.get(GetTradeSessionCommand(command.tradeSessionId))
        tradeSession.exit()
        tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
    }

}