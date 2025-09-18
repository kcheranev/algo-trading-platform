package com.github.trading.infra.adapter.income.web.rest.model.response

import com.github.trading.domain.model.CandleInterval
import java.util.UUID

data class StrategyConfigurationDto(
    val id: UUID,
    val type: String,
    val candleInterval: CandleInterval,
    val parameters: Map<String, Number>
)

data class StrategyConfigurationSearchResponseDto(
    val strategyConfigurations: List<StrategyConfigurationDto>
)