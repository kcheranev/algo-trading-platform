package ru.kcheranev.trading.infra.adapter.income.web.rest.model.common

import io.swagger.v3.oas.annotations.media.Schema

data class InstrumentDto(
    @Schema(description = "Id") val id: String,
    @Schema(description = "Ticker") val ticker: String
)