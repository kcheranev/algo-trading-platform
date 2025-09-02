package ru.kcheranev.trading.infra.adapter.income.web.ui.model.request

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.backtesting.ProfitTypeSort
import java.math.BigDecimal
import java.time.LocalDate

enum class CandlesDataSource {

    BROKER, FILE;

}

data class StrategyAnalyzeRequestUiDto(
    val strategyType: String? = null,
    val strategyParameters: MutableMap<String, StrategyParameterUiDto> = mutableMapOf(),
    val parametersMutation: StrategyParametersMutationUiDto = StrategyParametersMutationUiDto(),
    val resultFilter: StrategyAnalyzeResultFilterUiDto = StrategyAnalyzeResultFilterUiDto(),
    val profitTypeSort: ProfitTypeSort = ProfitTypeSort.NET,
    val brokerInstrumentId: String? = null,
    val candleInterval: CandleInterval? = null,
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val candlesSeriesSource: CandlesDataSource = CandlesDataSource.FILE,
    var candlesSeriesFileName: String? = null
)

data class StrategyParameterUiDto(
    val value: Number? = null,
    val mutable: Boolean? = null
)

data class CheckedValueUiDto(
    val value: BigDecimal,
    val checked: Boolean
)

data class StrategyAnalyzeResultFilterUiDto(
    val resultsLimit: Int = 15,
    val minProfitLossTradesRatio: CheckedValueUiDto = CheckedValueUiDto(BigDecimal(1), false),
    val tradesByDayCountFactor: CheckedValueUiDto = CheckedValueUiDto(BigDecimal(1), false)
)

data class StrategyParametersMutationUiDto(
    val divisionFactor: BigDecimal = BigDecimal(2),
    val variantsCount: Int = 5
)