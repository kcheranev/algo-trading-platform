package com.github.trading.infra.adapter.outcome.persistence.impl

import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import com.github.trading.core.port.outcome.persistence.instrument.GetInstrumentByBrokerInstrumentIdCommand
import com.github.trading.core.port.outcome.persistence.instrument.GetInstrumentCommand
import com.github.trading.core.port.outcome.persistence.instrument.InsertInstrumentCommand
import com.github.trading.core.port.outcome.persistence.instrument.InstrumentPersistencePort
import com.github.trading.infra.adapter.outcome.persistence.PersistenceNotFoundException
import com.github.trading.infra.adapter.outcome.persistence.persistenceOutcomeAdapterMapper
import com.github.trading.infra.adapter.outcome.persistence.repository.InstrumentRepository

@Component
class InstrumentPersistenceOutcomeAdapter(
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val instrumentRepository: InstrumentRepository
) : InstrumentPersistencePort {

    @Transactional(propagation = Propagation.MANDATORY)
    override fun insert(command: InsertInstrumentCommand) {
        jdbcTemplate.insert(persistenceOutcomeAdapterMapper.map(command.instrument))
    }

    override fun get(command: GetInstrumentCommand) =
        instrumentRepository.findById(command.instrumentId.value)
            .orElseThrow { PersistenceNotFoundException("Instrument entity with id ${command.instrumentId.value} is not exists") }
            .let(persistenceOutcomeAdapterMapper::map)

    override fun getByBrokerInstrumentId(command: GetInstrumentByBrokerInstrumentIdCommand) =
        instrumentRepository.getInstrumentByBrokerInstrumentId(command.brokerInstrumentId)
            .orElseThrow { PersistenceNotFoundException("Instrument entity with brokerInstrumentId ${command.brokerInstrumentId} is not exists") }
            .let(persistenceOutcomeAdapterMapper::map)

    override fun findAll() =
        instrumentRepository.findAll()
            .map(persistenceOutcomeAdapterMapper::map)

}