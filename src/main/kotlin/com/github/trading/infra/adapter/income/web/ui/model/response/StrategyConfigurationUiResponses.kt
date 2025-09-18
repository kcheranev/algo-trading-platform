package com.github.trading.infra.adapter.income.web.ui.model.response

import com.github.trading.domain.model.CandleInterval
import java.util.UUID

data class StrategyConfigurationUiDto(
    val id: UUID,
    val type: String,
    val candleInterval: CandleInterval,
    val parameters: Map<String, Number>
)