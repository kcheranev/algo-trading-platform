package com.github.trading.core.port.income.strategyconfiguration

import com.github.trading.common.Default
import com.github.trading.core.port.model.Page
import com.github.trading.core.port.model.sort.Sort
import com.github.trading.core.port.model.sort.StrategyConfigurationSort
import com.github.trading.domain.entity.StrategyConfigurationId
import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.StrategyParameters

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