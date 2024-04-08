package ru.kcheranev.trading.infra.adapter.income.web.model.response

import ru.kcheranev.trading.domain.model.TradeDirection
import java.math.BigDecimal
import java.time.LocalDateTime

data class StrategyAnalyzeDto(
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
    val trades: List<TradeDto>,
    val totalGrossProfit: BigDecimal,
    val totalNetProfit: BigDecimal
)

data class TradeDto(
    val entry: OrderDto,
    val exit: OrderDto,
    val profit: BigDecimal
)

data class OrderDto(
    val date: LocalDateTime,
    val direction: TradeDirection,
    val price: BigDecimal
)

data class StrategyAdjustAndAnalyzeDto(
    val result: StrategyAnalyzeDto,
    val params: Map<String, Int>
)

data class StrategyAdjustAndAnalyzeResponseDto(
    val analyzeResults: List<StrategyAdjustAndAnalyzeDto>
)