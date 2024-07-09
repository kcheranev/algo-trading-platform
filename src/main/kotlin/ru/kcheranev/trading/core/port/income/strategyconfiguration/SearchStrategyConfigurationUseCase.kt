package ru.kcheranev.trading.core.port.income.strategyconfiguration

import ru.kcheranev.trading.domain.entity.StrategyConfiguration

interface SearchStrategyConfigurationUseCase {

    fun search(command: SearchStrategyConfigurationCommand): List<StrategyConfiguration>

}