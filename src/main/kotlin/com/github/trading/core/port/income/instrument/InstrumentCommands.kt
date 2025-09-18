package com.github.trading.core.port.income.instrument

data class CreateInstrumentCommand(
    val name: String,
    val ticker: String,
    val lot: Int,
    val brokerInstrumentId: String
)