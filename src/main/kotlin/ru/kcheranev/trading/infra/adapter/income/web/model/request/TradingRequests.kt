package ru.kcheranev.trading.infra.adapter.income.web.model.request

import io.swagger.v3.oas.annotations.media.Schema
import ru.kcheranev.trading.domain.model.CandleInterval
import java.util.UUID

data class CreateStrategyConfigurationRequestDto(
    @Schema(description = "Type") val type: String,
    @Schema(description = "Initial candle amount") val initCandleAmount: Int,
    @Schema(description = "Candle interval") val candleInterval: CandleInterval,
    @Schema(description = "Strategy parameters") val params: Map<String, Int>
)

data class StartTradeSessionRequestDto(
    @Schema(description = "Strategy configuration id") val strategyConfigurationId: UUID,
    @Schema(description = "Lots quantity") val lotsQuantity: Int,
    @Schema(description = "Instrument") val instrument: InstrumentDto
)