package ru.kcheranev.trading.core.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import java.math.BigDecimal
import java.time.LocalTime

@ConfigurationProperties("application.trading")
data class TradingProperties @ConstructorBinding constructor(
    val availableDelayedCandleCount: Int,
    val placeOrderRetryCount: Int,
    val defaultCommission: BigDecimal,
    val startTradingTime: LocalTime,
    val endTradingTime: LocalTime
)