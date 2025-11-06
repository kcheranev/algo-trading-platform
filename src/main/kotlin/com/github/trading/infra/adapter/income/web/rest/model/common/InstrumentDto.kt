package com.github.trading.infra.adapter.income.web.rest.model.common

import io.swagger.v3.oas.annotations.media.Schema

data class InstrumentDto(
    @field:Schema(description = "Id") val id: String,
    @field:Schema(description = "Ticker") val ticker: String
)