package com.github.trading.infra.adapter.income.web.rest.model.request

import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.backtesting.ProfitTypeSort
import com.github.trading.infra.adapter.income.web.rest.model.common.InstrumentDto
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

data class StrategyAnalyzeRequestDto(
    @Schema(description = "Strategy type") val strategyType: String,
    @Schema(description = "Strategy parameters") val strategyParameters: Map<String, Number> = emptyMap(),
    @Schema(description = "Mutable strategy parameters") val mutableStrategyParameters: Map<String, Number> = emptyMap(),
    @Schema(description = "Parameters mutation") val parametersMutation: StrategyParametersMutationDto,
    @Schema(description = "Backtesting result filter") val resultFilter: StrategyAnalyzeResultFilterDto?,
    @Schema(description = "Profit type sort") val profitTypeSort: ProfitTypeSort?,
    @Schema(description = "Instrument") val instrument: InstrumentDto,
    @Schema(description = "Candle interval") val candleInterval: CandleInterval,
    @Schema(description = "Start of strategy backtesting period") val from: LocalDate,
    @Schema(description = "End of strategy backtesting period") val to: LocalDate
)

data class StrategyAnalyzeResultFilterDto(
    @Schema(description = "Results limit") val resultsLimit: Int?,
    @Schema(description = "Minimal profit loss trades ratio") val minProfitLossTradesRatio: BigDecimal?,
    @Schema(description = "Trades by day count factor") val tradesByDayCountFactor: BigDecimal?
)

data class StrategyParametersMutationDto(
    @Schema(description = "Division factor") var divisionFactor: BigDecimal,
    @Schema(description = "Variants count") var variantsCount: Int
)