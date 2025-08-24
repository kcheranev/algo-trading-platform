package ru.kcheranev.trading.infra.adapter.income.web.ui.model.request

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.backtesting.ProfitTypeSort
import java.math.BigDecimal
import java.time.LocalDate

enum class CandlesDataSource {

    BROKER, FILE;

}

data class StrategyAnalyzeRequestUiDto(
    var strategyType: String? = null,
    var strategyParameters: MutableMap<String, Number?> = mutableMapOf(),
    var brokerInstrumentId: String? = null,
    var candleInterval: CandleInterval? = null,
    var from: LocalDate? = null,
    var to: LocalDate? = null,
    val candlesSeriesSource: CandlesDataSource = CandlesDataSource.BROKER
)

data class StrategyParametersAnalyzeRequestUiDto(
    var strategyType: String? = null,
    var strategyParameters: MutableMap<String, StrategyParameterUiDto> = mutableMapOf(),
    var parametersMutation: StrategyParametersMutationUiDto = StrategyParametersMutationUiDto(),
    var resultFilter: StrategyAnalyzeResultFilterUiDto = StrategyAnalyzeResultFilterUiDto(),
    var profitTypeSort: ProfitTypeSort = ProfitTypeSort.NET,
    var brokerInstrumentId: String? = null,
    var candleInterval: CandleInterval? = null,
    var from: LocalDate? = null,
    var to: LocalDate? = null,
    val candlesSeriesSource: CandlesDataSource = CandlesDataSource.BROKER
)

data class StrategyParameterUiDto(
    var value: Number? = null,
    var mutable: Boolean? = null
)

data class CheckedValueUiDto(
    var value: BigDecimal,
    var checked: Boolean
)

data class StrategyAnalyzeResultFilterUiDto(
    var resultsLimit: Int = 15,
    var minProfitLossTradesRatio: CheckedValueUiDto = CheckedValueUiDto(BigDecimal(1), false),
    var tradesByDayCountFactor: CheckedValueUiDto = CheckedValueUiDto(BigDecimal(1), false)
)

data class StrategyParametersMutationUiDto(
    var divisionFactor: BigDecimal = BigDecimal(2),
    var variantsCount: Int = 5
)