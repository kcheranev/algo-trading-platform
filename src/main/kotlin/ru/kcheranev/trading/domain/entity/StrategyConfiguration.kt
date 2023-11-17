package ru.kcheranev.trading.domain.entity

import ru.kcheranev.trading.domain.AbstractEntity
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.StrategyType

data class StrategyConfiguration(
    val id: StrategyConfigurationId,
    val type: StrategyType,
    val initCandleAmount: Int,
    val candleInterval: CandleInterval,
    val params: StrategyParameters
) : AbstractEntity() {
}

data class StrategyConfigurationId(
    val value: Long
)