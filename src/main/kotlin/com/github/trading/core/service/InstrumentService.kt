package com.github.trading.core.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.github.trading.core.port.income.instrument.CreateInstrumentCommand
import com.github.trading.core.port.income.instrument.CreateInstrumentUseCase
import com.github.trading.core.port.income.instrument.FindAllInstrumentsUseCase
import com.github.trading.core.port.outcome.persistence.instrument.InsertInstrumentCommand
import com.github.trading.core.port.outcome.persistence.instrument.InstrumentPersistencePort
import com.github.trading.domain.entity.Instrument

@Service
class InstrumentService(
    private val instrumentPersistencePort: InstrumentPersistencePort
) : CreateInstrumentUseCase,
    FindAllInstrumentsUseCase {

    @Transactional
    override fun createInstrument(command: CreateInstrumentCommand) {
        val instrument =
            Instrument.create(
                name = command.name,
                ticker = command.ticker,
                lot = command.lot,
                brokerInstrumentId = command.brokerInstrumentId
            )
        instrumentPersistencePort.insert(InsertInstrumentCommand(instrument))
    }

    override fun findAll() = instrumentPersistencePort.findAll()

}