package ru.kcheranev.trading.domain.entity

import org.springframework.data.domain.AbstractAggregateRoot
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.StrategyType

data class StrategyConfiguration(
    val id: StrategyConfigurationId,
    val type: StrategyType,
    val initCandleAmount: Int,
    val candleInterval: CandleInterval,
    val params: StrategyParameters
) : AbstractAggregateRoot<StrategyConfiguration>() {
}

data class StrategyConfigurationId(
    val value: Long
)

enum class StrategyConfigurationSort : SortField {

    TYPE, CANDLE_INTERVAL

}