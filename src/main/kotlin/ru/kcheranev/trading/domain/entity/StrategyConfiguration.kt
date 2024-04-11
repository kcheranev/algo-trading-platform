package ru.kcheranev.trading.domain.entity

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyParameters
import java.util.UUID

data class StrategyConfiguration(
    val id: StrategyConfigurationId?,
    val type: String,
    val initCandleAmount: Int,
    val candleInterval: CandleInterval,
    val params: StrategyParameters
) : AbstractAggregateRoot() {

    companion object {

        fun create(
            type: String,
            initCandleAmount: Int,
            candleInterval: CandleInterval,
            params: Map<String, Number>
        ) = StrategyConfiguration(
            id = null,
            type = type,
            initCandleAmount = initCandleAmount,
            candleInterval = candleInterval,
            params = StrategyParameters(params)
        )

    }

}

data class StrategyConfigurationId(
    val value: UUID
)