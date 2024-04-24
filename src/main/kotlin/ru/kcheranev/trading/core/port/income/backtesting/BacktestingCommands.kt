package ru.kcheranev.trading.core.port.income.backtesting

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.StrategyParameters
import java.math.BigDecimal
import java.time.LocalDateTime

data class StrategyAnalyzeCommand(
    val strategyType: String,
    val strategyParams: StrategyParameters,
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val candlesFrom: LocalDateTime,
    val candlesTo: LocalDateTime
)

data class StrategyAdjustAndAnalyzeCommand(
    val strategyType: String,
    val strategyParams: StrategyParameters,
    val mutableStrategyParams: StrategyParameters,
    val adjustFactor: BigDecimal,
    val adjustVariantCount: Int,
    val resultsLimit: Int?,
    val minProfitLossPositionsRatio: BigDecimal?,
    val tradesByDayCountFactor: BigDecimal?,
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val candlesFrom: LocalDateTime,
    val candlesTo: LocalDateTime
)