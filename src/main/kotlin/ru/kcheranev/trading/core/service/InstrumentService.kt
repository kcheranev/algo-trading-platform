package ru.kcheranev.trading.core.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.port.income.instrument.CreateInstrumentCommand
import ru.kcheranev.trading.core.port.income.instrument.CreateInstrumentUseCase
import ru.kcheranev.trading.core.port.income.instrument.FindAllInstrumentsUseCase
import ru.kcheranev.trading.core.port.outcome.persistence.instrument.InsertInstrumentCommand
import ru.kcheranev.trading.core.port.outcome.persistence.instrument.InstrumentPersistencePort
import ru.kcheranev.trading.domain.entity.Instrument

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
                brokerInstrumentId = command.brokerInstrumentId
            )
        instrumentPersistencePort.insert(InsertInstrumentCommand(instrument))
    }

    override fun findAll() = instrumentPersistencePort.findAll()

}