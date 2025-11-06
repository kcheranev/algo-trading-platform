package com.github.trading.infra.adapter.income.web.rest.model.request

import com.github.trading.core.port.model.Page
import com.github.trading.core.port.model.sort.Sort
import com.github.trading.core.port.model.sort.StrategyConfigurationSort
import com.github.trading.domain.model.CandleInterval
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class CreateStrategyConfigurationRequestDto(
    @field:Schema(description = "Name") val name: String,
    @field:Schema(description = "Type") val type: String,
    @field:Schema(description = "Candle interval") val candleInterval: CandleInterval,
    @field:Schema(description = "Strategy parameters") val parameters: Map<String, Number>
)

data class SearchStrategyConfigurationRequestDto(
    val id: UUID? = null,
    val type: String? = null,
    val candleInterval: CandleInterval? = null,
    val page: Page? = null,
    val sort: Sort<StrategyConfigurationSort>? = null
)