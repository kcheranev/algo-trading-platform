package ru.kcheranev.trading.core.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import java.math.BigDecimal
import java.time.LocalTime

@ConfigurationProperties("application.trading")
data class TradingProperties @ConstructorBinding constructor(
    val availableDelayedCandlesCount: Int,
    val placeOrderRetryCount: Int,
    val defaultCommission: BigDecimal,
    val tradingSchedule: List<TradingScheduleInterval>
) {

    init {
        initInstance(this)
    }

    companion object {

        @Volatile
        private var _instance: TradingProperties? = null

        val tradingProperties
            get() = _instance ?: throw RuntimeException("Trading properties are not initialized yet")

        private fun initInstance(tradeProperties: TradingProperties) {
            if (_instance == null) {
                synchronized(this) {
                    if (_instance == null) {
                        _instance = tradeProperties
                    }
                }
            }
        }

    }

}

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