package ru.kcheranev.trading.infra.adapter.income.web.model.response

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyParameters
import java.util.UUID

data class StrategyConfigurationDto(
    var id: UUID,
    var type: String,
    var candleInterval: CandleInterval,
    var params: StrategyParameters
)