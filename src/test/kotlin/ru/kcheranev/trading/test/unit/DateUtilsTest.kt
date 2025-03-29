package ru.kcheranev.trading.test.unit

import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import ru.kcheranev.trading.common.date.DateSupplier
import ru.kcheranev.trading.common.date.isTradingTime
import ru.kcheranev.trading.core.config.TradingProperties
import ru.kcheranev.trading.core.config.TradingScheduleInterval
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalTime

class DateUtilsTest : FreeSpec({

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

    "should check trading time" - {
        data class TestParameters(
            val now: LocalDateTime,
            val isTradingTime: Boolean
        )
        withData(
            nameFn = { "current time = ${it.now}, result = ${it.isTradingTime}" },
            TestParameters(LocalDateTime.parse("2024-01-30T10:00:00"), false),
            TestParameters(LocalDateTime.parse("2024-01-30T14:00:00"), true),
            TestParameters(LocalDateTime.parse("2024-01-30T19:00:00"), false),
            TestParameters(LocalDateTime.parse("2024-01-28T14:00:00"), false)
        ) { (now, isTradingTime) ->
            //given
            mockkObject(DateSupplier)
            every { DateSupplier.currentDateTime() } returns now

            //when
            val result = isTradingTime()

            //then
            result shouldBe isTradingTime

            //cleanup
            unmockkObject(DateSupplier)
        }
    }

})