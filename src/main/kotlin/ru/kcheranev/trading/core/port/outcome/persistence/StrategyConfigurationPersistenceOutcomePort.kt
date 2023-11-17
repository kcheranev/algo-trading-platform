package ru.kcheranev.trading.core.port.outcome.persistence

import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId

interface StrategyConfigurationPersistenceOutcomePort {

    fun save(command: SaveStrategyConfigurationPersistenceOutcomeCommand): StrategyConfigurationId

    fun get(command: GetStrategyConfigurationPersistenceOutcomeCommand): StrategyConfiguration

}