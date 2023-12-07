package ru.kcheranev.trading.infra.adapter.outcome.persistence.impl

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.port.outcome.persistence.GetStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.outcome.persistence.SaveStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.outcome.persistence.StrategyConfigurationPersistencePort
import ru.kcheranev.trading.core.port.outcome.persistence.StrategyConfigurationSearchCommand
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.infra.adapter.outcome.persistence.StrategyConfigurationEntityNotExistsException
import ru.kcheranev.trading.infra.adapter.outcome.persistence.persistenceOutcomeAdapterMapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository

@Component
class StrategyConfigurationPersistenceOutcomeAdapter(
    private val strategyConfigurationRepository: StrategyConfigurationRepository
) : StrategyConfigurationPersistencePort {

    @Transactional(propagation = Propagation.MANDATORY)
    override fun save(command: SaveStrategyConfigurationCommand): StrategyConfigurationId {
        return StrategyConfigurationId(
            strategyConfigurationRepository.save(
                persistenceOutcomeAdapterMapper.map(command.strategyConfiguration)
            ).id!!
        )
    }

    override fun get(command: GetStrategyConfigurationCommand): StrategyConfiguration {
        return strategyConfigurationRepository.findById(command.strategyConfigurationId.value)
            .orElseThrow { StrategyConfigurationEntityNotExistsException(command.strategyConfigurationId) }
            .let { persistenceOutcomeAdapterMapper.map(it) }
    }

    override fun search(command: StrategyConfigurationSearchCommand): List<StrategyConfiguration> =
        strategyConfigurationRepository.search(command).map { persistenceOutcomeAdapterMapper.map(it) }

}