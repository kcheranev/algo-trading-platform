package ru.kcheranev.trading.infra.adapter.income.web.ui.model.response

import java.util.UUID

data class InstrumentUiResponseDto(
    val id: UUID,
    val name: String,
    val ticker: String,
    val brokerInstrumentId: String
)
