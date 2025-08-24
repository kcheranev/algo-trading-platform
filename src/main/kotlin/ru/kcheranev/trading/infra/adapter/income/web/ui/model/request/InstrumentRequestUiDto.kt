package ru.kcheranev.trading.infra.adapter.income.web.ui.model.request

data class CreateInstrumentRequestUiDto(
    var name: String? = null,
    var ticker: String? = null,
    var brokerInstrumentId: String? = null
)

data class InstrumentRequestUiDto(
    var id: String? = null,
    var ticker: String? = null
)