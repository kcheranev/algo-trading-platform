package com.github.trading.test.unit.domain

import com.github.trading.common.date.DateSupplier
import com.github.trading.common.date.toMskInstant
import com.github.trading.core.config.TradingProperties
import com.github.trading.core.config.TradingScheduleInterval
import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.TradeStrategy
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalTime

class TradeStrategyTest : FreeSpec({

    beforeSpec {
        mockkObject(TradingProperties.Companion)
        every { TradingProperties.tradingProperties } returns
                TradingProperties(
                    availableDelayedCandlesCount = 5,
                    placeOrderRetryCount = 3,
                    defaultCommission = BigDecimal("0.0004"),
                    tradingSchedule = listOf(
                        TradingScheduleInterval(LocalTime.parse("10:00:00"), LocalTime.parse("18:40:00")),
                        TradingScheduleInterval(LocalTime.parse("19:05:00"), LocalTime.parse("23:50:00"))
                    )
                )
    }

    afterSpec {
        unmockkObject(TradingProperties)
    }

    "should check is candle series fresh" - {
        data class TestParameters(
            val lastCandleDateTime: String,
            val targetDate: String,
            val candleInterval: CandleInterval,
            val fresh: Boolean
        )
        withData(
            nameFn = { "last candle date = ${it.lastCandleDateTime}, actual date = ${it.targetDate}, candle interval = ${it.candleInterval}, is fresh = ${it.fresh}" },
            TestParameters("2024-10-02T10:15:00", "2024-10-02T10:30:00", CandleInterval.ONE_MIN, false),
            TestParameters("2024-10-02T10:15:00", "2024-10-02T10:18:00", CandleInterval.ONE_MIN, true),
            TestParameters("2024-10-02T10:15:00", "2024-10-02T10:20:00", CandleInterval.ONE_MIN, true),
            TestParameters("2024-10-01T23:45:00", "2024-10-02T10:00:00", CandleInterval.ONE_MIN, false)
        ) { (lastCandleDateTime, now, candleInterval, isFreshCandleSeries) ->
            //given
            mockkObject(DateSupplier)
            every { DateSupplier.currentDateTime() } returns LocalDateTime.parse(now)
            val tradeStrategy =
                TradeStrategy(
                    series = mockk {
                        every { isEmpty } returns false
                        every { lastBar.endTime } returns LocalDateTime.parse(lastCandleDateTime).toMskInstant()
                    },
                    margin = false,
                    strategy = mockk()
                )

            //when
            val result = tradeStrategy.isFreshCandleSeries(candleInterval)

            //then
            result shouldBe isFreshCandleSeries

            //cleanup
            unmockkObject(DateSupplier)
        }
    }

})