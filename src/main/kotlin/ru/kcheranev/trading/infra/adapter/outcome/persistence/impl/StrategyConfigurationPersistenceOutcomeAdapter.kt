package ru.kcheranev.trading.infra.adapter.outcome.persistence.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.port.outcome.persistence.strategyconfiguration.GetStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.outcome.persistence.strategyconfiguration.InsertStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.outcome.persistence.strategyconfiguration.SaveStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.outcome.persistence.strategyconfiguration.SearchStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.outcome.persistence.strategyconfiguration.StrategyConfigurationPersistencePort
import ru.kcheranev.trading.infra.adapter.outcome.persistence.PersistenceNotFoundException
import ru.kcheranev.trading.infra.adapter.outcome.persistence.persistenceOutcomeAdapterMapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository

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