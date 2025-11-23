package com.github.trading.infra.adapter.income.web.rest.model.request

import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.backtesting.MutationDirection
import com.github.trading.domain.model.backtesting.ProfitTypeSort
import com.github.trading.infra.adapter.income.web.rest.model.common.InstrumentDto
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

data class StrategyAnalyzeRequestDto(
    @field:Schema(description = "Strategy type") val strategyType: String,
    @field:Schema(description = "Strategy parameters") val strategyParameters: Map<String, Number> = emptyMap(),
    @field:Schema(description = "Mutable strategy parameters") val mutableStrategyParameters: Map<String, MutableStrategyParameterRequestDto> = emptyMap(),
    @field:Schema(description = "Parameters mutation") val parametersMutation: StrategyParametersMutationDto,
    @field:Schema(description = "Backtesting result filter") val resultFilter: StrategyAnalyzeResultFilterDto?,
    @field:Schema(description = "Profit type sort") val profitTypeSort: ProfitTypeSort?,
    @field:Schema(description = "Instrument") val instrument: InstrumentDto,
    @field:Schema(description = "Candle interval") val candleInterval: CandleInterval,
    @field:Schema(description = "Start of strategy backtesting period") val from: LocalDate,
    @field:Schema(description = "End of strategy backtesting period") val to: LocalDate
)

data class MutableStrategyParameterRequestDto(
    val value: Number,
    val direction: MutationDirection
)

data class StrategyAnalyzeResultFilterDto(
    @field:Schema(description = "Results limit") val resultsLimit: Int?,
    @field:Schema(description = "Minimal profit loss trades ratio") val minProfitLossTradesRatio: BigDecimal?,
    @field:Schema(description = "Trades by day count factor") val tradesByDayCountFactor: BigDecimal?
)

data class StrategyParametersMutationDto(
    @field:Schema(description = "Division factor") var divisionFactor: BigDecimal,
    @field:Schema(description = "Variants count") var variantsCount: Int
)