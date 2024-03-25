package ru.kcheranev.trading.core.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("application.trading")
data class TradingProperties @ConstructorBinding constructor(
    val availableDelayedCandleCount: Int,
    val placeOrderRetryCount: Int
)