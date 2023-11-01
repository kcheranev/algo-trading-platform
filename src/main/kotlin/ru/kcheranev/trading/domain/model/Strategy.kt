package ru.kcheranev.trading.domain.model

data class Strategy(
    val type: StrategyType,
    val initCandleAmount: Int,
    val candleInterval: CandleInterval,
    val parameters: StrategyParameters,
    val algorithm: org.ta4j.core.Strategy
)