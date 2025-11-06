package com.github.trading.core.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.trading.core.config.TradingProperties.Companion.tradingProperties
import com.github.trading.core.port.income.backtesting.StrategyAnalyzeOnBrokerDataCommand
import com.github.trading.core.port.income.backtesting.StrategyAnalyzeOnStoredDataCommand
import com.github.trading.core.port.income.backtesting.StrategyAnalyzeUseCase
import com.github.trading.core.port.outcome.broker.GetHistoricCandlesForLongPeriodCommand
import com.github.trading.core.port.outcome.broker.HistoricCandleBrokerPort
import com.github.trading.core.strategy.factory.StrategyFactoryProvider
import com.github.trading.domain.model.Candle
import com.github.trading.domain.model.backtesting.Backtesting
import com.github.trading.domain.model.backtesting.StrategyParametersAnalyzeResult
import org.springframework.stereotype.Service

@Service
class BacktestingService(
    private val historicCandleBrokerPort: HistoricCandleBrokerPort,
    private val strategyFactoryProvider: StrategyFactoryProvider,
    private val objectMapper: ObjectMapper
) : StrategyAnalyzeUseCase {

    override fun analyzeStrategyOnBrokerData(command: StrategyAnalyzeOnBrokerDataCommand): List<StrategyParametersAnalyzeResult> {
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
        return backtesting.analyzeStrategy(
            strategyFactory = strategyFactory,
            parameters = command.strategyParameters,
            mutableParameters = command.mutableStrategyParameters,
            parametersMutation = command.parametersMutation,
            resultFilter = command.resultFilter,
            profitTypeSort = command.profitTypeSort
        )
    }

    override fun analyzeStrategyOnStoredData(command: StrategyAnalyzeOnStoredDataCommand): List<StrategyParametersAnalyzeResult> {
        val candles: List<Candle> = objectMapper.readValue(command.candlesSeriesFile.contentAsByteArray)
        val backtesting =
            Backtesting(
                name = "Backtesting on stored data ${command.candlesSeriesFile}",
                commission = tradingProperties.defaultCommission,
                candles = candles
            )
        val strategyFactory = strategyFactoryProvider.getStrategyFactory(command.strategyType)
        return backtesting.analyzeStrategy(
            strategyFactory = strategyFactory,
            parameters = command.strategyParameters,
            mutableParameters = command.mutableStrategyParameters,
            parametersMutation = command.parametersMutation,
            resultFilter = command.resultFilter,
            profitTypeSort = command.profitTypeSort
        )
    }

}