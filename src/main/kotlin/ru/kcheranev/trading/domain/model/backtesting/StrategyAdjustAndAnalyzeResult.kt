package ru.kcheranev.trading.domain.model.backtesting

data class StrategyAdjustAndAnalyzeResult(
    val result: StrategyAnalyzeResult,
    val params: Map<String, Int>
)