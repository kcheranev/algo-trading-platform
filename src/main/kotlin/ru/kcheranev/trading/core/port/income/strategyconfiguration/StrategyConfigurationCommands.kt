package ru.kcheranev.trading.core.port.income.strategyconfiguration

import ru.kcheranev.trading.common.Default
import ru.kcheranev.trading.core.port.model.Page
import ru.kcheranev.trading.core.port.model.sort.Sort
import ru.kcheranev.trading.core.port.model.sort.StrategyConfigurationSort
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyParameters

data class CreateStrategyConfigurationCommand(
    val name: String,
    val type: String,
    val candleInterval: CandleInterval,
    val parameters: StrategyParameters
)

data class SearchStrategyConfigurationCommand @Default constructor(
    val id: StrategyConfigurationId? = null,
    val type: String? = null,
    val candleInterval: CandleInterval? = null,
    val page: Page? = null,
    val sort: Sort<StrategyConfigurationSort>? = null
)