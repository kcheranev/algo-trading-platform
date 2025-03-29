package ru.kcheranev.trading.infra.adapter.income.web.ui.model.common

import java.util.UUID

data class InstrumentUiDto(
    val id: UUID,
    val name: String,
    val ticker: String,
    val brokerInstrumentId: String
)
