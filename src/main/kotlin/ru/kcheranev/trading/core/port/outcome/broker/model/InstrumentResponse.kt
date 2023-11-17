package ru.kcheranev.trading.core.port.outcome.broker.model

data class InstrumentResponse(
    val figi: String,
    val ticker: String,
    val isin: String,
    val lot: Int,
    val currency: String
)