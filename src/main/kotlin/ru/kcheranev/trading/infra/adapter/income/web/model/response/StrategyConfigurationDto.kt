package ru.kcheranev.trading.infra.adapter.income.web.model.response

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.StrategyType

data class StrategyConfigurationDto(
    var id: Long,
    var type: StrategyType,
    var initCandleAmount: Int,
    var candleInterval: CandleInterval,
    var params: StrategyParameters
)