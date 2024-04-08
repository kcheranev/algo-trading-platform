package ru.kcheranev.trading.domain.model.backtesting

import java.math.BigDecimal

data class StrategyAnalyzeResult(
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