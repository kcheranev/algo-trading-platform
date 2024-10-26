package ru.kcheranev.trading.domain.model.backtesting

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

data class StrategyParametersAnalyzeResult(
    val analyzeResult: StrategyAnalyzeResult,
    val parameters: Map<String, Number>
)

data class StrategyAnalyzeResult(
    val netProfit: BigDecimal,
    val grossProfit: BigDecimal,
    val netLoss: BigDecimal,
    val grossLoss: BigDecimal,
    val profitPositionsCount: Int,
    val losingPositionsCount: Int,
    val positionsCount: Int,
    val consecutiveProfitPositionsCount: Int,
    val consecutiveLosingPositionsCount: Int,
    val averageLoss: BigDecimal,
    val averageProfit: BigDecimal,
    val enterAndHoldReturn: BigDecimal,
    val maximumDrawdown: BigDecimal,
    val barsCount: Int,
    val profitLoss: BigDecimal,
    val profitLossPercentage: BigDecimal,
    val profitLossRatio: BigDecimal,
    val trades: List<Trade>
) {

    val grossValue = grossProfit + grossLoss

    val netValue = netProfit + netLoss

    val profitLossPositionsRatio: BigDecimal =
        if (positionsCount == 0) {
            BigDecimal.ZERO
        } else if (losingPositionsCount == 0) {
            BigDecimal(profitPositionsCount)
        } else {
            BigDecimal(profitPositionsCount)
                .divide(BigDecimal(losingPositionsCount), BACKTESTING_RESULT_SCALE, RoundingMode.HALF_UP)
        }

    fun splitByDay(): Map<LocalDate, DailyStrategyAnalyzeResult> =
        trades.groupBy { it.entry.date.toLocalDate() }
            .mapValues { DailyStrategyAnalyzeResult(it.value) }

}

data class DailyStrategyAnalyzeResult(
    val trades: List<Trade>
) {

    val netProfit = trades.sumOf { it.netProfit }

    val grossProfit = trades.sumOf { it.grossProfit }

    val netLoss = trades.sumOf { it.netLoss }

    val grossLoss = trades.sumOf { it.grossLoss }

    val grossValue = grossProfit + grossLoss

    val netValue = netProfit + netLoss

    val profitPositionsCount = trades.count { it.profitPosition() }

    val losingPositionsCount = trades.count { it.losingPosition() }

    val positionsCount = trades.count()

}