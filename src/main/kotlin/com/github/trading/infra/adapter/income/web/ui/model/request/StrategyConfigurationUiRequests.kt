package com.github.trading.infra.adapter.income.web.ui.model.request

import com.github.trading.core.port.model.Page
import com.github.trading.core.port.model.sort.Sort
import com.github.trading.core.port.model.sort.StrategyConfigurationSort
import com.github.trading.domain.model.CandleInterval
import java.util.UUID

data class CreateStrategyConfigurationRequestUiDto(
    val name: String,
    val type: String,
    val candleInterval: CandleInterval,
    val parameters: Map<String, Number>
)

data class SearchStrategyConfigurationRequestUiDto(
    val id: UUID? = null,
    val type: String? = null,
    val candleInterval: CandleInterval? = null,
    val page: Page? = null,
    val sort: Sort<StrategyConfigurationSort>? = null
)