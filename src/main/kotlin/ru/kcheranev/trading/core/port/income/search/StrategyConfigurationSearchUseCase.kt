package ru.kcheranev.trading.core.port.income.search

import ru.kcheranev.trading.domain.entity.StrategyConfiguration

interface StrategyConfigurationSearchUseCase {

    fun search(command: StrategyConfigurationSearchCommand): List<StrategyConfiguration>

}