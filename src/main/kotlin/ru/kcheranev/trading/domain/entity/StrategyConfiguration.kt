package ru.kcheranev.trading.domain.entity

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyParameters
import java.util.UUID

data class StrategyConfiguration(
    val id: StrategyConfigurationId,
    val name: String,
    val type: String,
    val candleInterval: CandleInterval,
    val parameters: StrategyParameters
) : AbstractAggregateRoot() {

    companion object {

        fun create(
            name: String,
            type: String,
            candleInterval: CandleInterval,
            parameters: StrategyParameters
        ) = StrategyConfiguration(
            id = StrategyConfigurationId.init(),
            name = name,
            type = type,
            candleInterval = candleInterval,
            parameters = parameters
        )

    }

}

data class StrategyConfigurationId(
    val value: UUID
) {

    override fun toString() = value.toString()

    companion object {

        fun init() = StrategyConfigurationId(UUID.randomUUID())

    }

}