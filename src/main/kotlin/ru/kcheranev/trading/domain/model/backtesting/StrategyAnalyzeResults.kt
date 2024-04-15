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

    val totalNumberOfProfitPositions = results.values.sumOf { it.numberOfProfitPositions }

    val totalNumberOfLosingPositions = results.values.sumOf { it.numberOfLosingPositions }

    val profitLossPositionsRatio: BigDecimal =
        if (totalNumberOfLosingPositions == 0) {
            BigDecimal.ZERO
        } else {
            BigDecimal(totalNumberOfProfitPositions)
                .divide(BigDecimal(totalNumberOfLosingPositions), 4, RoundingMode.HALF_UP)
        }

}

data class DailyStrategyAnalyzeResult(
    val averageLoss: BigDecimal,
    val averageProfit: BigDecimal,
    val enterAndHoldReturn: BigDecimal,
    val netLoss: BigDecimal,
    val grossLoss: BigDecimal,
    val maximumDrawdown: BigDecimal,
    val numberOfBars: Int,
    val numberOfConsecutiveProfitPositions: Int,
    val numberOfConsecutiveLosingPositions: Int,
    val numberOfLosingPositions: Int,
    val numberOfPositions: Int,
    val numberOfProfitPositions: Int,
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