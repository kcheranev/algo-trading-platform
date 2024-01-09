package ru.kcheranev.trading.infra.adapter.income.web.model.response

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyParameters

data class StrategyConfigurationDto(
    var id: Long,
    var type: String,
    var initCandleAmount: Int,
    var candleInterval: CandleInterval,
    var params: StrategyParameters
)