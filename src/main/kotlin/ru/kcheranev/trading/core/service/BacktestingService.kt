package ru.kcheranev.trading.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import ru.kcheranev.trading.core.config.TradingProperties.Companion.tradingProperties
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeOnBrokerDataCommand
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeOnStoredDataCommand
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeUseCase
import ru.kcheranev.trading.core.port.income.backtesting.StrategyParametersAnalyzeOnBrokerDataCommand
import ru.kcheranev.trading.core.port.income.backtesting.StrategyParametersAnalyzeOnStoredDataCommand
import ru.kcheranev.trading.core.port.outcome.broker.GetHistoricCandlesForLongPeriodCommand
import ru.kcheranev.trading.core.port.outcome.broker.HistoricCandleBrokerPort
import ru.kcheranev.trading.core.strategy.factory.StrategyFactoryProvider
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.backtesting.Backtesting
import ru.kcheranev.trading.domain.model.backtesting.StrategyAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.StrategyParametersAnalyzeResult

@Service
class BacktestingService(
    private val historicCandleBrokerPort: HistoricCandleBrokerPort,
    private val strategyFactoryProvider: StrategyFactoryProvider,
    private val objectMapper: ObjectMapper
) : StrategyAnalyzeUseCase {

    override fun analyzeStrategyOnBrokerData(command: StrategyAnalyzeOnBrokerDataCommand): StrategyAnalyzeResult {
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
                name = "Backtesting on broker data: ticker=${command.instrument.ticker}, candleInterval=${command.candleInterval}",
                commission = tradingProperties.defaultCommission,
                candles = candles
            )
        val strategyFactory = strategyFactoryProvider.getStrategyFactory(command.strategyType)
        return backtesting.analyzeStrategy(strategyFactory, StrategyParameters(command.strategyParameters))
    }

    override fun analyzeStrategyOnStoredData(command: StrategyAnalyzeOnStoredDataCommand): StrategyAnalyzeResult {
        val candles: List<Candle> = objectMapper.readValue(command.candlesSeriesFile.contentAsByteArray)
        val backtesting =
            Backtesting(
                name = "Backtesting on stored data ${command.candlesSeriesFile.filename}",
                commission = tradingProperties.defaultCommission,
                candles = candles
            )
        val strategyFactory = strategyFactoryProvider.getStrategyFactory(command.strategyType)
        return backtesting.analyzeStrategy(strategyFactory, StrategyParameters(command.strategyParameters))
    }

    override fun analyzeStrategyParametersOnBrokerData(command: StrategyParametersAnalyzeOnBrokerDataCommand): List<StrategyParametersAnalyzeResult> {
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
                name = "Backtesting on broker data: ticker=${command.instrument.ticker}, candleInterval=${command.candleInterval}",
                commission = tradingProperties.defaultCommission,
                candles = candles
            )
        val strategyFactory = strategyFactoryProvider.getStrategyFactory(command.strategyType)
        return backtesting.analyzeStrategyParameters(
            strategyFactory = strategyFactory,
            parameters = command.strategyParameters,
            mutableParameters = command.mutableStrategyParameters,
            parametersMutation = command.parametersMutation,
            resultFilter = command.resultFilter,
            profitTypeSort = command.profitTypeSort
        )
    }

    override fun analyzeStrategyParametersOnStoredData(command: StrategyParametersAnalyzeOnStoredDataCommand): List<StrategyParametersAnalyzeResult> {
        val candles: List<Candle> = objectMapper.readValue(command.candlesSeriesFile.contentAsByteArray)
        val backtesting =
            Backtesting(
                name = "Backtesting on stored data ${command.candlesSeriesFile}",
                commission = tradingProperties.defaultCommission,
                candles = candles
            )
        val strategyFactory = strategyFactoryProvider.getStrategyFactory(command.strategyType)
        return backtesting.analyzeStrategyParameters(
            strategyFactory = strategyFactory,
            parameters = command.strategyParameters,
            mutableParameters = command.mutableStrategyParameters,
            parametersMutation = command.parametersMutation,
            resultFilter = command.resultFilter,
            profitTypeSort = command.profitTypeSort
        )
    }

}