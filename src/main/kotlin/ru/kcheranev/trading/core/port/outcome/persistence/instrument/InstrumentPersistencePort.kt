package ru.kcheranev.trading.core.port.outcome.persistence.instrument

import ru.kcheranev.trading.domain.entity.Instrument

interface InstrumentPersistencePort {

    fun insert(command: InsertInstrumentCommand)

    fun findAll(): List<Instrument>

}