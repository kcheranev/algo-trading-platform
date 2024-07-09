package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom

import ru.kcheranev.trading.core.port.outcome.persistence.strategyconfiguration.SearchStrategyConfigurationCommand
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity

interface CustomizedStrategyConfigurationRepository {

    fun search(command: SearchStrategyConfigurationCommand): List<StrategyConfigurationEntity>

}