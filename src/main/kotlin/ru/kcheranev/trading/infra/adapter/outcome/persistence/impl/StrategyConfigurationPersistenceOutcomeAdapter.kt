package ru.kcheranev.trading.infra.adapter.outcome.persistence.impl

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.port.outcome.persistence.GetStrategyConfigurationPersistenceOutcomeCommand
import ru.kcheranev.trading.core.port.outcome.persistence.SaveStrategyConfigurationPersistenceOutcomeCommand
import ru.kcheranev.trading.core.port.outcome.persistence.StrategyConfigurationPersistenceOutcomePort
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.infra.adapter.outcome.persistence.StrategyConfigurationEntityNotExistsException
import ru.kcheranev.trading.infra.adapter.outcome.persistence.persistenceOutcomeAdapterMapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository

@Component
class StrategyConfigurationPersistenceOutcomeAdapter(
    private val strategyConfigurationRepository: StrategyConfigurationRepository
) : StrategyConfigurationPersistenceOutcomePort {

    @Transactional(propagation = Propagation.MANDATORY)
    override fun save(command: SaveStrategyConfigurationPersistenceOutcomeCommand): StrategyConfigurationId {
        return StrategyConfigurationId(
            strategyConfigurationRepository.save(
                persistenceOutcomeAdapterMapper.map(command.strategyConfiguration)
            ).id!!
        )
    }

    override fun get(command: GetStrategyConfigurationPersistenceOutcomeCommand): StrategyConfiguration {
        return strategyConfigurationRepository.findById(command.strategyConfigurationId.value)
            .orElseThrow { StrategyConfigurationEntityNotExistsException(command.strategyConfigurationId) }
            .let { persistenceOutcomeAdapterMapper.map(it) }
    }

}