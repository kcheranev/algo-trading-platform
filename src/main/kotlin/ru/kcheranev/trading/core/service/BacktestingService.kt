package ru.kcheranev.trading.core.service

import org.springframework.stereotype.Service
import ru.kcheranev.trading.core.config.TradingProperties
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAdjustAndAnalyzeCommand
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeCommand
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeUseCase
import ru.kcheranev.trading.core.port.outcome.broker.GetHistoricCandlesForLongPeriodCommand
import ru.kcheranev.trading.core.port.outcome.broker.HistoricCandleBrokerPort
import ru.kcheranev.trading.core.strategy.factory.StrategyFactoryProvider
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.backtesting.Backtesting
import ru.kcheranev.trading.domain.model.backtesting.ParametrizedStrategyResult
import ru.kcheranev.trading.domain.model.backtesting.StrategyAnalyzeResult

@Service
class BacktestingService(
    tradingProperties: TradingProperties,
    private val historicCandleBrokerPort: HistoricCandleBrokerPort,
    private val strategyFactoryProvider: StrategyFactoryProvider
) : StrategyAnalyzeUseCase {

    private val defaultCommission = tradingProperties.defaultCommission

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
                commission = defaultCommission,
                candles = candles
            )
        val strategyFactory = strategyFactoryProvider.getStrategyFactory(command.strategyType)
        return backtesting.analyzeStrategy(strategyFactory, StrategyParameters(command.strategyParameters))
    }

    override fun adjustAndAnalyzeStrategy(command: StrategyAdjustAndAnalyzeCommand): List<ParametrizedStrategyResult> {
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
                commission = defaultCommission,
                candles = candles
            )
        val strategyFactory = strategyFactoryProvider.getStrategyFactory(command.strategyType)
        return backtesting.adjustAndAnalyzeStrategy(
            strategyFactory = strategyFactory,
            parameters = command.strategyParameters,
            mutableParameters = command.mutableStrategyParameters,
            adjustFactor = command.adjustFactor,
            adjustVariantCount = command.adjustVariantCount,
            resultsLimit = command.resultFilter?.resultsLimit,
            minProfitLossPositionsRatio = command.resultFilter?.minProfitLossPositionsRatio,
            tradesByDayCountFactor = command.resultFilter?.tradesByDayCountFactor,
            profitTypeSort = command.profitTypeSort
        )
    }

}