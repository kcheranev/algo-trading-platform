package ru.kcheranev.trading.domain.entity

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyType

data class StrategyConfiguration(
    val id: StrategyConfigurationId,
    val type: StrategyType,
    val initCandleAmount: Int,
    val candleInterval: CandleInterval,
    val params: Map<String, String>
) {
}

data class StrategyConfigurationId(
    val value: Int
)