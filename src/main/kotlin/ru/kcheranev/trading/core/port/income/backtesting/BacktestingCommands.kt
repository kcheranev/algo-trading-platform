package ru.kcheranev.trading.core.port.income.backtesting

import org.springframework.core.io.Resource
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.backtesting.ProfitTypeSort
import java.math.BigDecimal
import java.time.LocalDate

data class StrategyAnalyzeOnBrokerDataCommand(
    val strategyType: String,
    val strategyParameters: StrategyParameters,
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val from: LocalDate,
    val to: LocalDate
)

data class StrategyAnalyzeOnStoredDataCommand(
    val strategyType: String,
    val strategyParameters: StrategyParameters,
    val candlesSeriesFile: Resource
)

data class StrategyParametersAnalyzeOnBrokerDataCommand(
    val strategyType: String,
    val strategyParameters: StrategyParameters,
    val mutableStrategyParameters: StrategyParameters,
    val parametersMutation: StrategyParametersMutation,
    val resultFilter: StrategyAnalyzeResultFilter?,
    val profitTypeSort: ProfitTypeSort?,
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val from: LocalDate,
    val to: LocalDate
)

data class StrategyParametersAnalyzeOnStoredDataCommand(
    val strategyType: String,
    val strategyParameters: StrategyParameters,
    val mutableStrategyParameters: StrategyParameters,
    val parametersMutation: StrategyParametersMutation,
    val resultFilter: StrategyAnalyzeResultFilter?,
    val profitTypeSort: ProfitTypeSort?,
    val candlesSeriesFile: Resource
)

data class StrategyAnalyzeResultFilter(
    val resultsLimit: Int?,
    val minProfitLossTradesRatio: BigDecimal?,
    val tradesByDayCountFactor: BigDecimal?
)

data class StrategyParametersMutation(
    var divisionFactor: BigDecimal,
    var variantsCount: Int
)