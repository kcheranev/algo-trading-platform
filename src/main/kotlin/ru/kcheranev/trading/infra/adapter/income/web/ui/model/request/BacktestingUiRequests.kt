package ru.kcheranev.trading.infra.adapter.income.web.ui.model.request

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.backtesting.ProfitTypeSort
import java.math.BigDecimal
import java.time.LocalDate

data class StrategyAnalyzeRequestUiDto(
    var strategyType: String? = null,
    var strategyParameters: MutableList<StrategyParameterUiDto> = mutableListOf(),
    var instrument: InstrumentRequestUiDto? = null,
    var candleInterval: CandleInterval? = null,
    var from: LocalDate? = null,
    var to: LocalDate? = null
)

data class StrategyParametersAnalyzeRequestUiDto(
    var strategyType: String?,
    var strategyParameters: MutableList<StrategyParameterUiDto> = mutableListOf(),
    var mutableStrategyParameters: MutableList<StrategyParameterUiDto> = mutableListOf(),
    var divisionFactor: BigDecimal?,
    var variantsCount: Int?,
    var resultFilter: StrategyAnalyzeResultFilterUiDto?,
    var profitTypeSort: ProfitTypeSort?,
    var instrument: InstrumentRequestUiDto?,
    var candleInterval: CandleInterval?,
    var from: LocalDate?,
    var to: LocalDate?
)

data class StrategyAnalyzeResultFilterUiDto(
    var resultsLimit: Int?,
    var minProfitLossPositionsRatio: BigDecimal?,
    var tradesByDayCountFactor: BigDecimal?
)