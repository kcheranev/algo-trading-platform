package ru.kcheranev.trading.core.port.income.backtesting

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import java.math.BigDecimal
import java.time.LocalDateTime

data class StrategyAnalyzeCommand(
    val strategyType: String,
    val strategyParams: Map<String, Int>,
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val candlesFrom: LocalDateTime,
    val candlesTo: LocalDateTime
)

data class StrategyAdjustAndAnalyzeCommand(
    val strategyType: String,
    val strategyParams: Map<String, Int>,
    val mutableStrategyParams: Map<String, Int>,
    val adjustFactor: BigDecimal,
    val adjustVariantCount: Int,
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val candlesFrom: LocalDateTime,
    val candlesTo: LocalDateTime
)