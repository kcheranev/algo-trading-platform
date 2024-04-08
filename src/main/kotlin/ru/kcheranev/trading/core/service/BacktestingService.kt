package ru.kcheranev.trading.core.service

import org.springframework.stereotype.Service
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAdjustAndAnalyzeCommand
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeCommand
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeUseCase
import ru.kcheranev.trading.core.port.outcome.broker.GetHistoricCandlesCommand
import ru.kcheranev.trading.core.port.outcome.broker.HistoricCandleBrokerPort
import ru.kcheranev.trading.core.strategy.StrategyFactoryProvider
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.backtesting.Backtesting
import ru.kcheranev.trading.domain.model.backtesting.StrategyAdjustAndAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.StrategyAnalyzeResult

@Service
class BacktestingService(
    private val historicCandleBrokerPort: HistoricCandleBrokerPort,
    private val strategyFactoryProvider: StrategyFactoryProvider
) : StrategyAnalyzeUseCase {

    override fun analyzeStrategy(command: StrategyAnalyzeCommand): StrategyAnalyzeResult {
        val candles =
            historicCandleBrokerPort.getHistoricCandles(
                GetHistoricCandlesCommand(
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
                candles = candles
            )
        val strategyFactory = strategyFactoryProvider.getStrategyFactory(command.strategyType)
        return backtesting.analyzeStrategy(strategyFactory, StrategyParameters(command.strategyParams))
    }

    override fun adjustAndAnalyzeStrategy(command: StrategyAdjustAndAnalyzeCommand): List<StrategyAdjustAndAnalyzeResult> {
        val candles =
            historicCandleBrokerPort.getHistoricCandles(
                GetHistoricCandlesCommand(
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
                candles = candles
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