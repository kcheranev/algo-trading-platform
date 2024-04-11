package ru.kcheranev.trading.infra.adapter.income.web.model.request

import io.swagger.v3.oas.annotations.media.Schema
import ru.kcheranev.trading.domain.model.CandleInterval
import java.math.BigDecimal
import java.time.LocalDateTime

data class StrategyAnalyzeRequestDto(
    @Schema(description = "Strategy type") val strategyType: String,
    @Schema(description = "Strategy parameters") val strategyParams: Map<String, Number>,
    @Schema(description = "Instrument") val instrument: InstrumentDto,
    @Schema(description = "Candle interval") val candleInterval: CandleInterval,
    @Schema(description = "Start of strategy backtesting period") val candlesFrom: LocalDateTime,
    @Schema(description = "End of strategy backtesting period") val candlesTo: LocalDateTime
)

data class StrategyAdjustAndAnalyzeRequestDto(
    @Schema(description = "Strategy type") val strategyType: String,
    @Schema(description = "Strategy parameters") val strategyParams: Map<String, Number>,
    @Schema(description = "Mutable strategy parameters") val mutableStrategyParams: Map<String, Number>,
    @Schema(description = "Adjust factor") val adjustFactor: BigDecimal,
    @Schema(description = "Adjust variant count") val adjustVariantCount: Int,
    @Schema(description = "Instrument") val instrument: InstrumentDto,
    @Schema(description = "Candle interval") val candleInterval: CandleInterval,
    @Schema(description = "Start of strategy backtesting period") val candlesFrom: LocalDateTime,
    @Schema(description = "End of strategy backtesting period") val candlesTo: LocalDateTime
)