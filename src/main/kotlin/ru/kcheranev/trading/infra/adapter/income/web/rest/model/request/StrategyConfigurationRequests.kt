package ru.kcheranev.trading.infra.adapter.income.web.rest.model.request

import io.swagger.v3.oas.annotations.media.Schema
import ru.kcheranev.trading.core.port.model.Page
import ru.kcheranev.trading.core.port.model.sort.Sort
import ru.kcheranev.trading.core.port.model.sort.StrategyConfigurationSort
import ru.kcheranev.trading.domain.model.CandleInterval
import java.util.UUID

data class CreateStrategyConfigurationRequestDto(
    @Schema(description = "Name") val name: String,
    @Schema(description = "Type") val type: String,
    @Schema(description = "Candle interval") val candleInterval: CandleInterval,
    @Schema(description = "Strategy parameters") val parameters: Map<String, Number>
)

data class SearchStrategyConfigurationRequestDto(
    val id: UUID? = null,
    val type: String? = null,
    val candleInterval: CandleInterval? = null,
    val page: Page? = null,
    val sort: Sort<StrategyConfigurationSort>? = null
)