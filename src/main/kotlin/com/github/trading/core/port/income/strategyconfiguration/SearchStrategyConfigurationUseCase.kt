package com.github.trading.core.port.income.strategyconfiguration

import com.github.trading.domain.entity.StrategyConfiguration

interface SearchStrategyConfigurationUseCase {

    fun search(command: SearchStrategyConfigurationCommand): List<StrategyConfiguration>

}