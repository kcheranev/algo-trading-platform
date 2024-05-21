package ru.kcheranev.trading.core.port.income.backtesting

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.backtesting.ProfitTypeSort
import java.math.BigDecimal
import java.time.LocalDate

data class StrategyAnalyzeCommand(
    val strategyType: String,
    val strategyParams: StrategyParameters,
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val from: LocalDate,
    val to: LocalDate
)

data class StrategyAdjustAndAnalyzeCommand(
    val strategyType: String,
    val strategyParams: StrategyParameters,
    val mutableStrategyParams: StrategyParameters,
    val adjustFactor: BigDecimal,
    val adjustVariantCount: Int,
    val resultFilter: StrategyAnalyzeResultFilter?,
    val profitTypeSort: ProfitTypeSort?,
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val from: LocalDate,
    val to: LocalDate
)

data class StrategyAnalyzeResultFilter(
    val resultsLimit: Int?,
    val minProfitLossPositionsRatio: BigDecimal?,
    val tradesByDayCountFactor: BigDecimal?
)