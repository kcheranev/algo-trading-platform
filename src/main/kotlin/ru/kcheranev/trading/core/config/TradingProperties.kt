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
    val tradingSchedule: List<TradingScheduleInterval>
)

data class TradingScheduleInterval(
    val from: LocalTime,
    val to: LocalTime
) {

    fun contains(time: LocalTime) = time in from..to

    fun before(time: LocalTime) = to < time

    fun beforeOrContains(time: LocalTime) = before(time) || contains(time)

    fun after(time: LocalTime) = from > time

    fun afterOrContains(time: LocalTime) = after(time) || contains(time)

}