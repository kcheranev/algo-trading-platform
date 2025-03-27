package ru.kcheranev.trading.core.port.outcome.persistence.instrument

import ru.kcheranev.trading.domain.entity.Instrument

data class InsertInstrumentCommand(
    val instrument: Instrument
)