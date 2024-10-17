package ru.kcheranev.trading.infra.adapter.income.web.rest.model.response

import ru.kcheranev.trading.domain.model.TradeDirection
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class StrategyParametersAnalyzeResponseDto(
    val analyzeResults: List<StrategyParametersAnalyzeResultDto>
)

data class StrategyAnalyzeResponseDto(
    val analyzeResult: StrategyAnalyzeResultDto
)

data class StrategyParametersAnalyzeResultDto(
    val parameters: Map<String, Number>,
    val result: StrategyAnalyzeResultDto
)

data class StrategyAnalyzeResultDto(
    val netValue: BigDecimal,
    val grossValue: BigDecimal,
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
    val profitLossPositionsRatio: BigDecimal,
    val results: Map<LocalDate, DailyStrategyAnalyzeResultDto>
)

data class DailyStrategyAnalyzeResultDto(
    val netValue: BigDecimal,
    val grossValue: BigDecimal,
    val netProfit: BigDecimal,
    val grossProfit: BigDecimal,
    val netLoss: BigDecimal,
    val grossLoss: BigDecimal,
    val profitPositionsCount: Int,
    val losingPositionsCount: Int,
    val positionsCount: Int,
    val trades: List<TradeDto>
)

data class TradeDto(
    val netValue: BigDecimal,
    val grossValue: BigDecimal,
    val entry: OrderDto,
    val exit: OrderDto?,
    val netProfit: BigDecimal,
    val grossProfit: BigDecimal,
    val netLoss: BigDecimal,
    val grossLoss: BigDecimal
)

data class OrderDto(
    val date: LocalDateTime,
    val direction: TradeDirection,
    val netPrice: BigDecimal,
    val grossPrice: BigDecimal
)