package ru.kcheranev.trading.infra.adapter.outcome.persistence.impl

import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.port.outcome.persistence.instrument.InsertInstrumentCommand
import ru.kcheranev.trading.core.port.outcome.persistence.instrument.InstrumentPersistencePort
import ru.kcheranev.trading.infra.adapter.outcome.persistence.persistenceOutcomeAdapterMapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.InstrumentRepository

@Component
class InstrumentPersistenceOutcomeAdapter(
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val instrumentRepository: InstrumentRepository
) : InstrumentPersistencePort {

    @Transactional(propagation = Propagation.MANDATORY)
    override fun insert(command: InsertInstrumentCommand) {
        jdbcTemplate.insert(persistenceOutcomeAdapterMapper.map(command.instrument))
    }

    override fun findAll() =
        instrumentRepository.findAll()
            .map(persistenceOutcomeAdapterMapper::map)

}