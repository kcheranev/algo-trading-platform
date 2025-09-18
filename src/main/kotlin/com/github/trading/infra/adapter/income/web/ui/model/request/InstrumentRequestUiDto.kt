package com.github.trading.infra.adapter.income.web.ui.model.request

data class CreateInstrumentRequestUiDto(
    val name: String? = null,
    val ticker: String? = null,
    val lot: Int? = null,
    val brokerInstrumentId: String? = null
)

data class InstrumentRequestUiDto(
    val id: String? = null,
    val ticker: String? = null
)