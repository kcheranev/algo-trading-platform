package ru.kcheranev.trading.infra.adapter.income.web.ui.model.request

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.backtesting.ProfitTypeSort
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.common.InstrumentUiDto
import java.math.BigDecimal
import java.time.LocalDate

data class StrategyAnalyzeRequestUiDto(
    val strategyType: String,
    val strategyParams: Map<String, Number>,
    val instrument: InstrumentUiDto,
    val candleInterval: CandleInterval,
    val from: LocalDate,
    val to: LocalDate
)

data class StrategyAdjustAndAnalyzeRequestUiDto(
    val strategyType: String,
    val strategyParams: Map<String, Number> = emptyMap(),
    val mutableStrategyParams: Map<String, Number> = emptyMap(),
    val adjustFactor: BigDecimal,
    val adjustVariantCount: Int,
    val resultFilter: StrategyAnalyzeResultFilterUiDto?,
    val profitTypeSort: ProfitTypeSort?,
    val instrument: InstrumentUiDto,
    val candleInterval: CandleInterval,
    val from: LocalDate,
    val to: LocalDate
)

data class StrategyAnalyzeResultFilterUiDto(
    val resultsLimit: Int?,
    val minProfitLossPositionsRatio: BigDecimal?,
    val tradesByDayCountFactor: BigDecimal?
)