package com.github.trading.core.port.outcome.persistence.strategyconfiguration

import com.github.trading.domain.entity.StrategyConfiguration

interface StrategyConfigurationPersistencePort {

    fun insert(command: InsertStrategyConfigurationCommand)

    fun save(command: SaveStrategyConfigurationCommand)

    fun get(command: GetStrategyConfigurationCommand): StrategyConfiguration

    fun search(command: SearchStrategyConfigurationCommand): List<StrategyConfiguration>

}