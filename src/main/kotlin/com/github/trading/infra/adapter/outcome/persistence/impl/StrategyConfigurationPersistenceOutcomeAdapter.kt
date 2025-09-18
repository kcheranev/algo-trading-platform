package com.github.trading.infra.adapter.outcome.persistence.impl

import com.github.trading.core.port.outcome.persistence.strategyconfiguration.GetStrategyConfigurationCommand
import com.github.trading.core.port.outcome.persistence.strategyconfiguration.InsertStrategyConfigurationCommand
import com.github.trading.core.port.outcome.persistence.strategyconfiguration.SaveStrategyConfigurationCommand
import com.github.trading.core.port.outcome.persistence.strategyconfiguration.SearchStrategyConfigurationCommand
import com.github.trading.core.port.outcome.persistence.strategyconfiguration.StrategyConfigurationPersistencePort
import com.github.trading.infra.adapter.outcome.persistence.PersistenceNotFoundException
import com.github.trading.infra.adapter.outcome.persistence.persistenceOutcomeAdapterMapper
import com.github.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class StrategyConfigurationPersistenceOutcomeAdapter(
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val strategyConfigurationRepository: StrategyConfigurationRepository,
    private val eventPublisher: ApplicationEventPublisher
) : StrategyConfigurationPersistencePort {

    @Transactional(propagation = Propagation.MANDATORY)
    override fun insert(command: InsertStrategyConfigurationCommand) {
        val strategyConfiguration = command.strategyConfiguration
        jdbcTemplate.insert(persistenceOutcomeAdapterMapper.map(strategyConfiguration))
        strategyConfiguration.events.forEach { eventPublisher.publishEvent(it) }
        strategyConfiguration.clearEvents()
    }

    @Transactional(propagation = Propagation.MANDATORY)
    override fun save(command: SaveStrategyConfigurationCommand) {
        val strategyConfiguration = command.strategyConfiguration
        strategyConfigurationRepository.save(persistenceOutcomeAdapterMapper.map(strategyConfiguration))
        strategyConfiguration.events.forEach { eventPublisher.publishEvent(it) }
        strategyConfiguration.clearEvents()
    }

    override fun get(command: GetStrategyConfigurationCommand) =
        strategyConfigurationRepository.findById(command.strategyConfigurationId.value)
            .orElseThrow {
                PersistenceNotFoundException("Strategy configuration entity with id ${command.strategyConfigurationId.value} is not exists")
            }
            .let(persistenceOutcomeAdapterMapper::map)

    override fun search(command: SearchStrategyConfigurationCommand) =
        strategyConfigurationRepository.search(command)
            .map(persistenceOutcomeAdapterMapper::map)

}