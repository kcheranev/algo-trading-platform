package ru.kcheranev.trading.core.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.common.LoggerDelegate
import ru.kcheranev.trading.core.port.income.ProcessIncomeCandleCommand
import ru.kcheranev.trading.core.port.income.ReceiveCandleUseCase
import ru.kcheranev.trading.core.port.income.StartTradeSessionCommand
import ru.kcheranev.trading.core.port.income.StartTradeSessionUseCase
import ru.kcheranev.trading.core.port.outcome.broker.GetLastHistoricCandlesCommand
import ru.kcheranev.trading.core.port.outcome.broker.HistoricCandleBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.OrderServiceBrokerPort
import ru.kcheranev.trading.core.port.outcome.persistence.GetStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.outcome.persistence.SaveTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.StrategyConfigurationPersistencePort
import ru.kcheranev.trading.core.port.outcome.persistence.TradeSessionPersistencePort
import ru.kcheranev.trading.core.strategy.StrategyFactoryProvider
import ru.kcheranev.trading.domain.entity.TradeSession

@Service
class TradeService(
    private val strategyConfigurationPersistenceOutcomePort: StrategyConfigurationPersistencePort,
    private val tradeSessionPersistencePort: TradeSessionPersistencePort,
    private val historicCandleBrokerPort: HistoricCandleBrokerPort,
    private val orderServiceBrokerOutcomePort: OrderServiceBrokerPort,
    private val strategyFactoryProvider: StrategyFactoryProvider
) : StartTradeSessionUseCase, ReceiveCandleUseCase {

    @Transactional
    override fun startTradeSession(command: StartTradeSessionCommand) {
        logger.info("Start trade session ${command.ticker} ${command.strategyType}")
        val strategyConfiguration =
            strategyConfigurationPersistenceOutcomePort.get(
                GetStrategyConfigurationCommand(command.strategyConfigurationId)
            )
        val strategyFactory = strategyFactoryProvider.getStrategyFactory(command.strategyType)
        val candles =
            historicCandleBrokerPort.getLastHistoricCandles(
                GetLastHistoricCandlesCommand(
                    command.ticker,
                    command.instrumentId,
                    strategyConfiguration.candleInterval,
                    strategyConfiguration.initCandleAmount
                )
            )
        val tradeSession = TradeSession.start(
            strategyConfiguration = strategyConfiguration,
            ticker = command.ticker,
            instrumentId = command.instrumentId,
            candles = candles,
            strategyFactory = strategyFactory
        )
        tradeSessionPersistencePort.save(SaveTradeSessionCommand(tradeSession))
    }

    @Transactional
    override fun processIncomeCandle(command: ProcessIncomeCandleCommand) {
        TODO("Not yet implemented")
    }

    companion object {

        private val logger by LoggerDelegate()

    }

}