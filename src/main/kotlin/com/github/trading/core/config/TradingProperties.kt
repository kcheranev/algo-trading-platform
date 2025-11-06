package com.github.trading.core.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import java.math.BigDecimal

@ConfigurationProperties("application.trading")
data class TradingProperties @ConstructorBinding constructor(
    val availableDelayedCandlesCount: Int,
    val placeOrderRetryCount: Int,
    val defaultCommission: BigDecimal
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