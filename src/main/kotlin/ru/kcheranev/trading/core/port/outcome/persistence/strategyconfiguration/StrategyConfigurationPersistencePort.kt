package ru.kcheranev.trading.core.port.outcome.persistence.strategyconfiguration

import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId

interface StrategyConfigurationPersistencePort {

    fun save(command: SaveStrategyConfigurationCommand): StrategyConfigurationId

    fun get(command: GetStrategyConfigurationCommand): StrategyConfiguration

    fun search(command: SearchStrategyConfigurationCommand): List<StrategyConfiguration>

}