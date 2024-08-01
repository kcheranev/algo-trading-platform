package ru.kcheranev.trading.infra.adapter.income.web.ui.model.request

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.backtesting.ProfitTypeSort
import java.math.BigDecimal
import java.time.LocalDate

data class StrategyAnalyzeRequestUiDto(
    var strategyType: String? = null,
    var strategyParams: MutableList<StrategyParamDto> = mutableListOf(),
    var instrument: InstrumentUiRequestDto? = null,
    var candleInterval: CandleInterval? = null,
    var from: LocalDate? = null,
    var to: LocalDate? = null
)

data class StrategyAdjustAndAnalyzeRequestUiDto(
    var strategyType: String?,
    var strategyParams: MutableList<StrategyParamDto> = mutableListOf(),
    var mutableStrategyParams: MutableList<StrategyParamDto> = mutableListOf(),
    var adjustFactor: BigDecimal?,
    var adjustVariantCount: Int?,
    var resultFilter: StrategyAnalyzeResultFilterUiDto?,
    var profitTypeSort: ProfitTypeSort?,
    var instrument: InstrumentUiRequestDto?,
    var candleInterval: CandleInterval?,
    var from: LocalDate?,
    var to: LocalDate?
)

data class StrategyAnalyzeResultFilterUiDto(
    var resultsLimit: Int?,
    var minProfitLossPositionsRatio: BigDecimal?,
    var tradesByDayCountFactor: BigDecimal?
)