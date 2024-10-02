package ru.kcheranev.trading.infra.adapter.income.web.rest.model.response

import ru.kcheranev.trading.domain.model.CandleInterval
import java.util.UUID

data class StrategyConfigurationDto(
    var id: UUID,
    var type: String,
    var candleInterval: CandleInterval,
    var parameters: Map<String, Number>
)

data class StrategyConfigurationSearchResponseDto(
    var strategyConfigurations: List<StrategyConfigurationDto>
)