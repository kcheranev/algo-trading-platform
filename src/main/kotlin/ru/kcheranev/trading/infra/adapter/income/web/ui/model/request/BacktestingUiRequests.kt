package ru.kcheranev.trading.infra.adapter.income.web.ui.model.request

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.backtesting.ProfitTypeSort
import java.math.BigDecimal
import java.time.LocalDate

data class StrategyAnalyzeRequestUiDto(
    var strategyType: String? = null,
    var strategyParameters: MutableMap<String, Number?> = mutableMapOf(),
    var instrument: InstrumentRequestUiDto? = null,
    var candleInterval: CandleInterval? = null,
    var from: LocalDate? = null,
    var to: LocalDate? = null
)

data class StrategyParametersAnalyzeRequestUiDto(
    var strategyType: String? = null,
    var strategyParameters: MutableMap<String, StrategyParameterUiDto> = mutableMapOf(),
    var divisionFactor: BigDecimal = BigDecimal(2),
    var variantsCount: Int = 5,
    var resultFilter: StrategyAnalyzeResultFilterUiDto = StrategyAnalyzeResultFilterUiDto(),
    var profitTypeSort: ProfitTypeSort = ProfitTypeSort.NET,
    var instrument: InstrumentRequestUiDto? = null,
    var candleInterval: CandleInterval? = null,
    var from: LocalDate? = null,
    var to: LocalDate? = null
)

data class StrategyParameterUiDto(
    var value: Number? = null,
    var mutable: Boolean? = null
)

data class StrategyAnalyzeResultFilterUiDto(
    var resultsLimit: Int = 15,
    var minProfitLossTradesRatio: BigDecimal = BigDecimal(1),
    var tradesByDayCountFactor: BigDecimal = BigDecimal(1)
)