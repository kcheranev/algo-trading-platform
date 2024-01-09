package ru.kcheranev.trading.domain.entity

import org.springframework.data.domain.AbstractAggregateRoot
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyParameters

data class StrategyConfiguration(
    val id: StrategyConfigurationId?,
    val type: String,
    val initCandleAmount: Int,
    val candleInterval: CandleInterval,
    val params: StrategyParameters
) : AbstractAggregateRoot<StrategyConfiguration>() {

    companion object {

        fun create(
            type: String,
            initCandleAmount: Int,
            candleInterval: CandleInterval,
            params: Map<String, Any>
        ): StrategyConfiguration =
            StrategyConfiguration(
                id = null,
                type = type,
                initCandleAmount = initCandleAmount,
                candleInterval = candleInterval,
                params = StrategyParameters(params)
            )

    }

}

data class StrategyConfigurationId(
    val value: Long
)

enum class StrategyConfigurationSort : SortField {

    TYPE, CANDLE_INTERVAL

}