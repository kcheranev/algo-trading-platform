package ru.kcheranev.trading.infra.adapter.income.web.ui.model.response

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyParameters
import java.util.UUID

data class StrategyConfigurationUiDto(
    val id: UUID,
    val type: String,
    val candleInterval: CandleInterval,
    val params: StrategyParameters
)