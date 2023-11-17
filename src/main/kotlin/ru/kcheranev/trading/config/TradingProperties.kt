package ru.kcheranev.trading.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("application.trading")
data class TradingProperties @ConstructorBinding constructor(
    val maxCandleDelay: Int,
    val placeOrderRetryCount: Int
)