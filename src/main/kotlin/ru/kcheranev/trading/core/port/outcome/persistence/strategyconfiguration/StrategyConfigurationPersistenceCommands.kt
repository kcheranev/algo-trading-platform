package ru.kcheranev.trading.core.port.outcome.persistence.strategyconfiguration

import ru.kcheranev.trading.core.port.model.Page
import ru.kcheranev.trading.core.port.model.sort.Sort
import ru.kcheranev.trading.core.port.model.sort.StrategyConfigurationSort
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.model.CandleInterval

data class InsertStrategyConfigurationCommand(
    val strategyConfiguration: StrategyConfiguration
)

data class SaveStrategyConfigurationCommand(
    val strategyConfiguration: StrategyConfiguration
)

data class GetStrategyConfigurationCommand(
    val strategyConfigurationId: StrategyConfigurationId
)

data class SearchStrategyConfigurationCommand(
    val id: StrategyConfigurationId?,
    val type: String?,
    val candleInterval: CandleInterval?,
    val page: Page?,
    val sort: Sort<StrategyConfigurationSort>?
)