package com.github.trading.infra.adapter.outcome.persistence.repository.custom

import com.github.trading.core.port.outcome.persistence.strategyconfiguration.SearchStrategyConfigurationCommand
import com.github.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity

interface CustomizedStrategyConfigurationRepository {

    fun search(command: SearchStrategyConfigurationCommand): List<StrategyConfigurationEntity>

}