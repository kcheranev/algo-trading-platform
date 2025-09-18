package com.github.trading.infra.adapter.income.web.ui.model.response

import com.github.trading.domain.model.TradeDirection
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

data class StrategyParametersAnalyzeResultUiDto(
    val parameters: Map<String, Number>,
    val analyzeResult: StrategyAnalyzeResultUiDto
)

data class StrategyAnalyzeResultUiDto(
    val netValue: BigDecimal,
    val grossValue: BigDecimal,
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
    val profitLossTradesRatio: BigDecimal,
    val strategyAnalyzeResultByMonth: Map<YearMonth, MonthlyStrategyAnalyzeResultUiDto>
)

data class MonthlyStrategyAnalyzeResultUiDto(
    val netProfit: BigDecimal,
    val grossProfit: BigDecimal,
    val netLoss: BigDecimal,
    val grossLoss: BigDecimal,
    val grossValue: BigDecimal,
    val netValue: BigDecimal,
    val profitTradesCount: Int,
    val losingTradesCount: Int,
    val tradesCount: Int,
    val strategyAnalyzeResultByDay: Map<LocalDate, DailyStrategyAnalyzeResultUiDto>
)

data class DailyStrategyAnalyzeResultUiDto(
    val netValue: BigDecimal,
    val grossValue: BigDecimal,
    val netProfit: BigDecimal,
    val grossProfit: BigDecimal,
    val netLoss: BigDecimal,
    val grossLoss: BigDecimal,
    val profitTradesCount: Int,
    val losingTradesCount: Int,
    val tradesCount: Int,
    val trades: List<TradeUiDto>
)

data class TradeUiDto(
    val netValue: BigDecimal,
    val grossValue: BigDecimal,
    val entry: OrderUiDto,
    val exit: OrderUiDto?,
    val netProfit: BigDecimal,
    val grossProfit: BigDecimal,
    val netLoss: BigDecimal,
    val grossLoss: BigDecimal,
)

data class OrderUiDto(
    val date: LocalDateTime,
    val direction: TradeDirection,
    val netPrice: BigDecimal,
    val grossPrice: BigDecimal
)

data class ErrorsUiDto(
    val errors: List<String> = emptyList(),
    val fieldErrors: Map<String, List<String>> = emptyMap()
)