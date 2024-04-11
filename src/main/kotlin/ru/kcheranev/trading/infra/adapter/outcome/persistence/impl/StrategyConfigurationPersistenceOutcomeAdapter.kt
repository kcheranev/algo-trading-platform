package ru.kcheranev.trading.infra.adapter.outcome.persistence.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.port.outcome.persistence.GetStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.outcome.persistence.SaveStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.outcome.persistence.StrategyConfigurationPersistencePort
import ru.kcheranev.trading.core.port.outcome.persistence.StrategyConfigurationSearchCommand
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.infra.adapter.outcome.persistence.PersistenceNotFoundException
import ru.kcheranev.trading.infra.adapter.outcome.persistence.persistenceOutcomeAdapterMapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository

@Component
class StrategyConfigurationPersistenceOutcomeAdapter(
    private val strategyConfigurationRepository: StrategyConfigurationRepository,
    private val eventPublisher: ApplicationEventPublisher
) : StrategyConfigurationPersistencePort {

    @Transactional(propagation = Propagation.MANDATORY)
    override fun save(command: SaveStrategyConfigurationCommand): StrategyConfigurationId {
        val strategyConfiguration = command.strategyConfiguration
        val strategyConfigurationId =
            StrategyConfigurationId(
                strategyConfigurationRepository.save(
                    persistenceOutcomeAdapterMapper.map(strategyConfiguration)
                ).id!!
            )
        strategyConfiguration.events.forEach { eventPublisher.publishEvent(it) }
        strategyConfiguration.clearEvents()
        return strategyConfigurationId
    }


    override fun get(command: GetStrategyConfigurationCommand) =
        strategyConfigurationRepository.findById(command.strategyConfigurationId.value)
            .orElseThrow {
                PersistenceNotFoundException("Strategy configuration entity with id ${command.strategyConfigurationId.value} is not exists")
            }
            .let { persistenceOutcomeAdapterMapper.map(it) }


    override fun search(command: StrategyConfigurationSearchCommand) =
        strategyConfigurationRepository.search(command).map { persistenceOutcomeAdapterMapper.map(it) }

}