package ru.kcheranev.trading.core.service

import org.springframework.stereotype.Service
import ru.kcheranev.trading.core.config.TradingProperties
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAdjustAndAnalyzeCommand
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeCommand
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeUseCase
import ru.kcheranev.trading.core.port.outcome.broker.GetHistoricCandlesForLongPeriodCommand
import ru.kcheranev.trading.core.port.outcome.broker.HistoricCandleBrokerPort
import ru.kcheranev.trading.core.strategy.StrategyFactoryProvider
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.backtesting.Backtesting
import ru.kcheranev.trading.domain.model.backtesting.PeriodStrategyAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.StrategyAdjustAndAnalyzeResult

@Service
class BacktestingService(
    tradingProperties: TradingProperties,
    private val historicCandleBrokerPort: HistoricCandleBrokerPort,
    private val strategyFactoryProvider: StrategyFactoryProvider
) : StrategyAnalyzeUseCase {

    private val defaultCommission = tradingProperties.defaultCommission

    override fun analyzeStrategy(command: StrategyAnalyzeCommand): PeriodStrategyAnalyzeResult {
        val candlesByPeriod =
            historicCandleBrokerPort.getHistoricCandlesForLongPeriod(
                GetHistoricCandlesForLongPeriodCommand(
                    instrument = command.instrument,
                    candleInterval = command.candleInterval,
                    from = command.candlesFrom,
                    to = command.candlesTo
                )
            )
        val backtesting =
            Backtesting(
                ticker = command.instrument.ticker,
                candleInterval = command.candleInterval,
                commission = defaultCommission,
                candlesByPeriod = candlesByPeriod
            )
        val strategyFactory = strategyFactoryProvider.getStrategyFactory(command.strategyType)
        return backtesting.analyzeStrategy(strategyFactory, StrategyParameters(command.strategyParams))
    }

    override fun adjustAndAnalyzeStrategy(command: StrategyAdjustAndAnalyzeCommand): List<StrategyAdjustAndAnalyzeResult> {
        val candlesByPeriod =
            historicCandleBrokerPort.getHistoricCandlesForLongPeriod(
                GetHistoricCandlesForLongPeriodCommand(
                    instrument = command.instrument,
                    candleInterval = command.candleInterval,
                    from = command.candlesFrom,
                    to = command.candlesTo
                )
            )
        val backtesting =
            Backtesting(
                ticker = command.instrument.ticker,
                candleInterval = command.candleInterval,
                commission = defaultCommission,
                candlesByPeriod = candlesByPeriod
            )
        val strategyFactory = strategyFactoryProvider.getStrategyFactory(command.strategyType)
        return backtesting.adjustAndAnalyzeStrategy(
            strategyFactory = strategyFactory,
            params = StrategyParameters(command.strategyParams),
            mutableParams = StrategyParameters(command.mutableStrategyParams),
            adjustFactor = command.adjustFactor,
            adjustVariantCount = command.adjustVariantCount
        )
    }

}