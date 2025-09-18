package com.github.trading.domain.model.backtesting

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

data class StrategyParametersAnalyzeResult(
    val analyzeResult: StrategyAnalyzeResult,
    val parameters: Map<String, Number>
)

data class StrategyAnalyzeResult(
    val netProfit: BigDecimal,
    val grossProfit: BigDecimal,
    val netLoss: BigDecimal,
    val grossLoss: BigDecimal,
    val profitTradesCount: Int,
    val losingTradesCount: Int,
    val tradesCount: Int,
    val consecutiveProfitTradesCount: Int,
    val consecutiveLosingTradesCount: Int,
    val averageLoss: BigDecimal,
    val averageProfit: BigDecimal,
    val enterAndHoldReturn: BigDecimal,
    val maximumDrawdown: BigDecimal,
    val barsCount: Int,
    val profitLoss: BigDecimal,
    val profitLossPercentage: BigDecimal,
    val profitLossRatio: BigDecimal,
    val strategyAnalyzeResultByMonth: Map<YearMonth, MonthlyStrategyAnalyzeResult>
) {

    val grossValue = grossProfit + grossLoss

    val netValue = netProfit + netLoss

    val profitLossTradesRatio: BigDecimal =
        if (tradesCount == 0) {
            BigDecimal.ZERO
        } else if (losingTradesCount == 0) {
            BigDecimal(profitTradesCount)
        } else {
            BigDecimal(profitTradesCount)
                .divide(BigDecimal(losingTradesCount), BACKTESTING_RESULT_SCALE, RoundingMode.HALF_UP)
        }

    companion object {

        fun from(
            netProfit: BigDecimal,
            grossProfit: BigDecimal,
            netLoss: BigDecimal,
            grossLoss: BigDecimal,
            profitTradesCount: Int,
            losingTradesCount: Int,
            tradesCount: Int,
            consecutiveProfitTradesCount: Int,
            consecutiveLosingTradesCount: Int,
            averageLoss: BigDecimal,
            averageProfit: BigDecimal,
            enterAndHoldReturn: BigDecimal,
            maximumDrawdown: BigDecimal,
            barsCount: Int,
            profitLoss: BigDecimal,
            profitLossPercentage: BigDecimal,
            profitLossRatio: BigDecimal,
            trades: List<Trade>
        ): StrategyAnalyzeResult {
            val strategyAnalyzeResultByMonth =
                trades.groupBy { YearMonth.from(it.entry.date.toLocalDate()) }
                    .mapValues { MonthlyStrategyAnalyzeResult.from(it.value) }
            return StrategyAnalyzeResult(
                netProfit = netProfit,
                grossProfit = grossProfit,
                netLoss = netLoss,
                grossLoss = grossLoss,
                profitTradesCount = profitTradesCount,
                losingTradesCount = losingTradesCount,
                tradesCount = tradesCount,
                consecutiveProfitTradesCount = consecutiveProfitTradesCount,
                consecutiveLosingTradesCount = consecutiveLosingTradesCount,
                averageLoss = averageLoss,
                averageProfit = averageProfit,
                enterAndHoldReturn = enterAndHoldReturn,
                maximumDrawdown = maximumDrawdown,
                barsCount = barsCount,
                profitLoss = profitLoss,
                profitLossPercentage = profitLossPercentage,
                profitLossRatio = profitLossRatio,
                strategyAnalyzeResultByMonth = strategyAnalyzeResultByMonth
            )
        }

    }

}

data class MonthlyStrategyAnalyzeResult(
    val strategyAnalyzeResultByDay: Map<LocalDate, DailyStrategyAnalyzeResult>
) {

    val netProfit: BigDecimal

    val grossProfit: BigDecimal

    val netLoss: BigDecimal

    val grossLoss: BigDecimal

    val grossValue: BigDecimal

    val netValue: BigDecimal

    val profitTradesCount: Int

    val losingTradesCount: Int

    val tradesCount: Int

    init {
        val dailyStrategyAnalyzeResults = strategyAnalyzeResultByDay.values
        netProfit = dailyStrategyAnalyzeResults.sumOf { it.netProfit }
        grossProfit = dailyStrategyAnalyzeResults.sumOf { it.grossProfit }
        netLoss = dailyStrategyAnalyzeResults.sumOf { it.netLoss }
        grossLoss = dailyStrategyAnalyzeResults.sumOf { it.grossLoss }
        grossValue = grossProfit + grossLoss
        netValue = netProfit + netLoss
        val trades = dailyStrategyAnalyzeResults.flatMap { it.trades }
        profitTradesCount = trades.count { it.profitPosition() }
        losingTradesCount = trades.count { it.losingPosition() }
        tradesCount = trades.count()
    }

    companion object {

        fun from(trades: List<Trade>) =
            MonthlyStrategyAnalyzeResult(
                trades.groupBy { it.entry.date.toLocalDate() }
                    .mapValues { DailyStrategyAnalyzeResult(it.value) }
            )

    }

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

    val profitTradesCount = trades.count { it.profitPosition() }

    val losingTradesCount = trades.count { it.losingPosition() }

    val tradesCount = trades.count()

}