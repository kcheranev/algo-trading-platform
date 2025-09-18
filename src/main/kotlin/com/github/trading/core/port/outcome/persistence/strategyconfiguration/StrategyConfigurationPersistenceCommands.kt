package com.github.trading.core.port.outcome.persistence.strategyconfiguration

import com.github.trading.core.port.model.Page
import com.github.trading.core.port.model.sort.Sort
import com.github.trading.core.port.model.sort.StrategyConfigurationSort
import com.github.trading.domain.entity.StrategyConfiguration
import com.github.trading.domain.entity.StrategyConfigurationId
import com.github.trading.domain.model.CandleInterval

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