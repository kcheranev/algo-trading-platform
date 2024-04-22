package ru.kcheranev.trading.infra.adapter.income.web.model.response

import ru.kcheranev.trading.domain.model.TradeDirection
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class StrategyAdjustAndAnalyzeResponseDto(
    val adjustedAnalyzeResults: List<StrategyAdjustAndAnalyzeDto>
)

data class StrategyAdjustAndAnalyzeDto(
    val params: Map<String, Number>,
    val result: PeriodStrategyAnalyzeResultDto
)

data class PeriodStrategyAnalyzeResultDto(
    val totalGrossProfit: BigDecimal,
    val totalNetProfit: BigDecimal,
    val profitPositionsTotalCount: Int,
    val losingPositionsTotalCount: Int,
    val profitLossPositionsRatio: BigDecimal,
    val notClosedPositionsCount: Int,
    val tradesCount: Int,
    val results: Map<LocalDate, DailyStrategyAnalyzeResultDto>
)

data class DailyStrategyAnalyzeResultDto(
    val totalGrossProfit: BigDecimal,
    val totalNetProfit: BigDecimal,
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
    val trades: List<TradeDto>
)

data class TradeDto(
    val entry: OrderDto,
    val exit: OrderDto?,
    val netProfit: BigDecimal?,
    val grossProfit: BigDecimal?
)

data class OrderDto(
    val date: LocalDateTime,
    val direction: TradeDirection,
    val netPrice: BigDecimal,
    val grossPrice: BigDecimal
)