package ru.kcheranev.trading.infra.adapter.income.web.ui.model.response

import ru.kcheranev.trading.domain.model.CandleInterval
import java.util.UUID

data class StrategyConfigurationUiDto(
    val id: UUID,
    val type: String,
    val candleInterval: CandleInterval,
    val parameters: Map<String, Number>
)