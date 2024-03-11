package ru.kcheranev.trading.core.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("application.trading")
data class TradingProperties @ConstructorBinding constructor(
    val candleDelaysProperties: CandleDelaysProperties,
    val placeOrderRetryCount: Int
)

data class CandleDelaysProperties(
    val availableCount: Int,
    val maxAvailableCount: Int
)