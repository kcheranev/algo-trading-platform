package ru.kcheranev.trading.core.port.outcome.persistence.instrument

import ru.kcheranev.trading.domain.entity.Instrument
import ru.kcheranev.trading.domain.entity.InstrumentId

data class InsertInstrumentCommand(
    val instrument: Instrument
)

data class GetInstrumentCommand(
    val instrumentId: InstrumentId
)

data class GetInstrumentByBrokerInstrumentIdCommand(
    val brokerInstrumentId: String
)