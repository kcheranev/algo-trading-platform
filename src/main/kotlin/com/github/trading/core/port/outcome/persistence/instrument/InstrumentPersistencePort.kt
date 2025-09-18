package com.github.trading.core.port.outcome.persistence.instrument

import com.github.trading.domain.entity.Instrument

interface InstrumentPersistencePort {

    fun insert(command: InsertInstrumentCommand)

    fun get(command: GetInstrumentCommand): Instrument

    fun getByBrokerInstrumentId(command: GetInstrumentByBrokerInstrumentIdCommand): Instrument

    fun findAll(): List<Instrument>

}