package ru.kcheranev.trading.infra.adapter.income.web.model.request

import io.swagger.v3.oas.annotations.media.Schema
import ru.kcheranev.trading.domain.model.CandleInterval
import java.time.LocalDateTime

sealed class BacktestingRequest

data class StrategyAnalyzeRequest(
    @Schema(description = "Strategy type") val strategyType: String,
    @Schema(description = "Strategy parameters") val strategyParams: Map<String, Any>,
    @Schema(description = "Instrument") val instrument: InstrumentDto,
    @Schema(description = "Candle interval") val candleInterval: CandleInterval,
    @Schema(description = "Start of strategy backtesting period") val candlesFrom: LocalDateTime,
    @Schema(description = "End of strategy backtesting period") val candlesTo: LocalDateTime
) : BacktestingRequest()