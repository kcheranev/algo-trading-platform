package ru.kcheranev.trading.infra.adapter.income.web.ui.model.response

import ru.kcheranev.trading.domain.model.TradeDirection
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class StrategyParametersAnalyzeResultUiDto(
    val parameters: Map<String, Number>,
    val analyzeResult: StrategyAnalyzeResultUiDto
) {

    val tradesCount =
        analyzeResult.results
            .values
            .flatMap { it.trades }
            .count()

}

data class StrategyAnalyzeResultUiDto(
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
    val results: Map<LocalDate, DailyStrategyAnalyzeResultUiDto>
)

data class DailyStrategyAnalyzeResultUiDto(
    val netValue: BigDecimal,
    val grossValue: BigDecimal,
    val netProfit: BigDecimal,
    val grossProfit: BigDecimal,
    val netLoss: BigDecimal,
    val grossLoss: BigDecimal,
    val profitPositionsCount: Int,
    val losingPositionsCount: Int,
    val positionsCount: Int,
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