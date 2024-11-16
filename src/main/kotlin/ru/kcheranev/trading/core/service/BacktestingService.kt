package ru.kcheranev.trading.core.service

import org.springframework.stereotype.Service
import ru.kcheranev.trading.core.config.TradingProperties.Companion.tradingProperties
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeCommand
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeUseCase
import ru.kcheranev.trading.core.port.income.backtesting.StrategyParametersAnalyzeCommand
import ru.kcheranev.trading.core.port.outcome.broker.GetHistoricCandlesForLongPeriodCommand
import ru.kcheranev.trading.core.port.outcome.broker.HistoricCandleBrokerPort
import ru.kcheranev.trading.core.strategy.factory.StrategyFactoryProvider
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.backtesting.Backtesting
import ru.kcheranev.trading.domain.model.backtesting.StrategyAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.StrategyParametersAnalyzeResult

@Service
class BacktestingService(
    private val historicCandleBrokerPort: HistoricCandleBrokerPort,
    private val strategyFactoryProvider: StrategyFactoryProvider
) : StrategyAnalyzeUseCase {

    override fun analyzeStrategy(command: StrategyAnalyzeCommand): StrategyAnalyzeResult {
        val candles =
            historicCandleBrokerPort.getHistoricCandlesForLongPeriod(
                GetHistoricCandlesForLongPeriodCommand(
                    instrument = command.instrument,
                    candleInterval = command.candleInterval,
                    from = command.from,
                    to = command.to
                )
            )
        val backtesting =
            Backtesting(
                ticker = command.instrument.ticker,
                candleInterval = command.candleInterval,
                commission = tradingProperties.defaultCommission,
                candles = candles
            )
        val strategyFactory = strategyFactoryProvider.getStrategyFactory(command.strategyType)
        return backtesting.analyzeStrategy(strategyFactory, StrategyParameters(command.strategyParameters))
    }

    override fun analyzeStrategyParameters(command: StrategyParametersAnalyzeCommand): List<StrategyParametersAnalyzeResult> {
        val candles =
            historicCandleBrokerPort.getHistoricCandlesForLongPeriod(
                GetHistoricCandlesForLongPeriodCommand(
                    instrument = command.instrument,
                    candleInterval = command.candleInterval,
                    from = command.from,
                    to = command.to
                )
            )
        val backtesting =
            Backtesting(
                ticker = command.instrument.ticker,
                candleInterval = command.candleInterval,
                commission = tradingProperties.defaultCommission,
                candles = candles
            )
        val strategyFactory = strategyFactoryProvider.getStrategyFactory(command.strategyType)
        return backtesting.analyzeStrategyParameters(
            strategyFactory = strategyFactory,
            parameters = command.strategyParameters,
            mutableParameters = command.mutableStrategyParameters,
            divisionFactor = command.divisionFactor,
            variantsCount = command.variantsCount,
            resultsLimit = command.resultFilter?.resultsLimit,
            minProfitLossTradesRatio = command.resultFilter?.minProfitLossTradesRatio,
            tradesByDayCountFactor = command.resultFilter?.tradesByDayCountFactor,
            profitTypeSort = command.profitTypeSort
        )
    }

}