package ru.kcheranev.trading.test.unit.domain

import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import ru.kcheranev.trading.core.config.TradingProperties
import ru.kcheranev.trading.core.config.TradingScheduleInterval
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.TradeStrategy
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
            TestParameters("2024-10-02T18:35:00", "2024-10-02T19:05:00", CandleInterval.ONE_MIN, true),
            TestParameters("2024-10-02T18:35:00", "2024-10-02T19:10:00", CandleInterval.ONE_MIN, false),
            TestParameters("2024-10-02T18:35:00", "2024-10-03T10:15:00", CandleInterval.ONE_MIN, false),
            TestParameters("2024-10-02T18:35:00", "2024-10-04T10:15:00", CandleInterval.ONE_MIN, false),
            TestParameters("2024-10-04T18:35:00", "2024-10-07T10:15:00", CandleInterval.ONE_MIN, false),
            TestParameters("2024-10-02T10:15:00", "2024-10-02T10:30:00", CandleInterval.FIVE_MIN, true),
            TestParameters("2024-10-02T10:15:00", "2024-10-02T10:34:00", CandleInterval.FIVE_MIN, true),
            TestParameters("2024-10-02T18:35:00", "2024-10-02T19:05:00", CandleInterval.FIVE_MIN, true),
            TestParameters("2024-10-02T18:35:00", "2024-10-02T19:10:00", CandleInterval.FIVE_MIN, true),
            TestParameters("2024-10-02T18:35:00", "2024-10-03T10:15:00", CandleInterval.FIVE_MIN, false),
            TestParameters("2024-10-02T18:35:00", "2024-10-04T10:15:00", CandleInterval.FIVE_MIN, false),
            TestParameters("2024-10-04T18:35:00", "2024-10-07T10:15:00", CandleInterval.FIVE_MIN, false)
        ) { (lastCandleDateTime, targetDate, candleInterval, isFreshCandleSeries) ->
            //given
            mockk<TradeStrategy> {
                every { lastCandleDate() } returns LocalDateTime.parse(lastCandleDateTime)
            }
            val tradeStrategy =
                TradeStrategy(
                    series = mockk {
                        every { isEmpty } returns false
                        every { lastBar.endTime.toLocalDateTime() } returns LocalDateTime.parse(lastCandleDateTime)
                    },
                    margin = false,
                    strategy = mockk()
                )

            //when
            val result = tradeStrategy.isFreshCandleSeries(LocalDateTime.parse(targetDate), candleInterval)

            //then
            result shouldBe isFreshCandleSeries
        }
    }

})