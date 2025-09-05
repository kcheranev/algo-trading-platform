package ru.kcheranev.trading.domain.entity

import java.util.UUID

data class Instrument(
    val id: InstrumentId,
    val name: String,
    val ticker: String,
    val lot: Int,
    val brokerInstrumentId: String
) {

    companion object {

        fun create(
            name: String,
            ticker: String,
            lot: Int,
            brokerInstrumentId: String
        ) = Instrument(
            id = InstrumentId.init(),
            name = name,
            ticker = ticker,
            lot = lot,
            brokerInstrumentId = brokerInstrumentId
        )

    }

}

data class InstrumentId(
    val value: UUID
) {

    override fun toString() = value.toString()

    companion object {

        fun init() = InstrumentId(UUID.randomUUID())

    }

}