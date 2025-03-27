package ru.kcheranev.trading.core.port.income.instrument

data class CreateInstrumentCommand(
    val name: String,
    val ticker: String,
    val brokerInstrumentId: String
)