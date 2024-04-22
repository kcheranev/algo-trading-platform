package ru.kcheranev.trading.domain.model.backtesting

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

data class StrategyAdjustAndAnalyzeResult(
    val result: PeriodStrategyAnalyzeResult,
    val params: Map<String, Number>
)

data class PeriodStrategyAnalyzeResult(
    val results: Map<LocalDate, DailyStrategyAnalyzeResult>
) {

    val totalGrossProfit = results.values.sumOf { it.totalGrossProfit }

    val totalNetProfit = results.values.sumOf { it.totalNetProfit }

    val profitPositionsTotalCount = results.values.sumOf { it.profitPositionsCount }

    val losingPositionsTotalCount = results.values.sumOf { it.losingPositionsCount }

    val profitLossPositionsRatio: BigDecimal =
        if (losingPositionsTotalCount == 0) {
            BigDecimal.ZERO
        } else {
            BigDecimal(profitPositionsTotalCount)
                .divide(BigDecimal(losingPositionsTotalCount), BACKTESTING_RESULT_SCALE, RoundingMode.HALF_UP)
        }

    val notClosedPositionsCount =
        results.values
            .count { result -> result.trades.any { trade -> trade.exit == null } }

    val tradesCount = profitPositionsTotalCount + losingPositionsTotalCount

}

data class DailyStrategyAnalyzeResult(
    val averageLoss: BigDecimal,
    val averageProfit: BigDecimal,
    val enterAndHoldReturn: BigDecimal,
    val netLoss: BigDecimal,
    val grossLoss: BigDecimal,
    val maximumDrawdown: BigDecimal,
    val barsCount: Int,
    val consecutiveProfitPositionsCount: Int,
    val consecutiveLosingPositionsCount: Int,
    val losingPositionsCount: Int,
    val positionsCount: Int,
    val profitPositionsCount: Int,
    val netProfit: BigDecimal,
    val grossProfit: BigDecimal,
    val profitLoss: BigDecimal,
    val profitLossPercentage: BigDecimal,
    val profitLossRatio: BigDecimal,
    val trades: List<Trade>
) {

    val totalGrossProfit = grossProfit + grossLoss

    val totalNetProfit = netProfit + netLoss

}