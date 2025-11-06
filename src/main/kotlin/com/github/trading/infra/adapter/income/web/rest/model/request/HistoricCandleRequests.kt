package com.github.trading.infra.adapter.income.web.rest.model.request

import com.github.trading.domain.model.CandleInterval
import com.github.trading.infra.adapter.income.web.rest.model.common.InstrumentDto
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class StoreHistoricCandlesRequestDto(
    @field:Schema(description = "Instrument") val instrument: InstrumentDto,
    @field:Schema(description = "From") val from: LocalDate,
    @field:Schema(description = "To") val to: LocalDate,
    @field:Schema(description = "Candle interval") val candleInterval: CandleInterval
)