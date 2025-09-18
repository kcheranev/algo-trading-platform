package com.github.trading.infra.adapter.income.web.rest.model.request

import io.swagger.v3.oas.annotations.media.Schema
import com.github.trading.domain.model.CandleInterval
import com.github.trading.infra.adapter.income.web.rest.model.common.InstrumentDto
import java.time.LocalDate

data class StoreHistoricCandlesRequestDto(
    @Schema(description = "Instrument") val instrument: InstrumentDto,
    @Schema(description = "From") val from: LocalDate,
    @Schema(description = "To") val to: LocalDate,
    @Schema(description = "Candle interval") val candleInterval: CandleInterval
)