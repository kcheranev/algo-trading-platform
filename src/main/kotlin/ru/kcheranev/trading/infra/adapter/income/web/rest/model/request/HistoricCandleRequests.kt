package ru.kcheranev.trading.infra.adapter.income.web.rest.model.request

import io.swagger.v3.oas.annotations.media.Schema
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.common.InstrumentDto
import java.time.LocalDate

data class StoreHistoricCandlesRequestDto(
    @Schema(description = "Instrument") val instrument: InstrumentDto,
    @Schema(description = "From") val from: LocalDate,
    @Schema(description = "To") val to: LocalDate,
    @Schema(description = "Candle interval") val candleInterval: CandleInterval
)