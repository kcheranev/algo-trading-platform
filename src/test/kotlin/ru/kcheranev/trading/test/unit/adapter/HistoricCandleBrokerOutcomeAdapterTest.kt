package ru.kcheranev.trading.test.unit.adapter

import com.google.protobuf.Timestamp
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.cache.CacheManager
import org.springframework.cache.support.NoOpCache
import ru.kcheranev.trading.common.date.DateSupplier
import ru.kcheranev.trading.common.date.atEndOfDay
import ru.kcheranev.trading.common.date.toMskInstant
import ru.kcheranev.trading.core.port.outcome.broker.GetLastHistoricCandlesCommand
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.infra.adapter.outcome.broker.impl.HistoricCandleBrokerOutcomeAdapter
import ru.tinkoff.piapi.contract.v1.HistoricCandle
import ru.tinkoff.piapi.core.MarketDataService
import ru.tinkoff.piapi.core.utils.MapperUtils.bigDecimalToQuotation
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class HistoricCandleBrokerOutcomeAdapterTest : StringSpec({

    fun buildCandle(
        high: BigDecimal,
        low: BigDecimal,
        open: BigDecimal,
        close: BigDecimal,
        time: LocalDateTime
    ) = HistoricCandle.newBuilder()
        .setLow(bigDecimalToQuotation(low))
        .setHigh(bigDecimalToQuotation(high))
        .setOpen(bigDecimalToQuotation(open))
        .setClose(bigDecimalToQuotation(close))
        .setIsComplete(true)
        .setTime(Timestamp.newBuilder().setSeconds(time.toMskInstant().epochSecond))
        .setVolume(100)
        .build()

    "should get last historic candles in one day" {
        //given
        val now = LocalDateTime.parse("2024-01-30T07:17:00")
        val dateSupplier =
            mockk<DateSupplier> {
                every { currentDateTime() } returns now
            }
        val cacheManager =
            mockk<CacheManager> {
                every { getCache(any()) } returns NoOpCache("cache-name")
            }
        val marketDataService =
            mockk<MarketDataService> {
                every {
                    getCandlesSync(
                        eq("e6123145-9665-43e0-8413-cd61b8aa9b1"),
                        eq(LocalDate.parse("2024-01-30").atStartOfDay().toMskInstant()),
                        eq(now.toMskInstant()),
                        eq(ru.tinkoff.piapi.contract.v1.CandleInterval.CANDLE_INTERVAL_1_MIN)
                    )
                } returns listOf(
                    buildCandle(
                        high = BigDecimal("96"),
                        low = BigDecimal("94"),
                        open = BigDecimal("95"),
                        close = BigDecimal("96"),
                        time = LocalDateTime.parse("2024-01-30T07:12:00")
                    ),
                    buildCandle(
                        high = BigDecimal("97"),
                        low = BigDecimal("95"),
                        open = BigDecimal("96"),
                        close = BigDecimal("97"),
                        time = LocalDateTime.parse("2024-01-30T07:13:00")
                    ),
                    buildCandle(
                        high = BigDecimal("98"),
                        low = BigDecimal("96"),
                        open = BigDecimal("97"),
                        close = BigDecimal("98"),
                        time = LocalDateTime.parse("2024-01-30T07:14:00")
                    ),
                    buildCandle(
                        high = BigDecimal("99"),
                        low = BigDecimal("97"),
                        open = BigDecimal("98"),
                        close = BigDecimal("99"),
                        time = LocalDateTime.parse("2024-01-30T07:15:00")
                    )
                )
            }
        val adapter = HistoricCandleBrokerOutcomeAdapter(marketDataService, dateSupplier, cacheManager)

        //when
        val candles =
            adapter.getLastHistoricCandles(
                GetLastHistoricCandlesCommand(
                    instrument = Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                    candleInterval = CandleInterval.ONE_MIN,
                    quantity = 4
                )
            )

        //then
        candles shouldBe listOf(
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal("95.000000000"),
                closePrice = BigDecimal("96.000000000"),
                highestPrice = BigDecimal("96.000000000"),
                lowestPrice = BigDecimal("94.000000000"),
                volume = 100,
                endDateTime = LocalDateTime.parse("2024-01-30T07:13:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            ),
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal("96.000000000"),
                closePrice = BigDecimal("97.000000000"),
                highestPrice = BigDecimal("97.000000000"),
                lowestPrice = BigDecimal("95.000000000"),
                volume = 100,
                endDateTime = LocalDateTime.parse("2024-01-30T07:14:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            ),
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal("97.000000000"),
                closePrice = BigDecimal("98.000000000"),
                highestPrice = BigDecimal("98.000000000"),
                lowestPrice = BigDecimal("96.000000000"),
                volume = 100,
                endDateTime = LocalDateTime.parse("2024-01-30T07:15:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            ),
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal("98.000000000"),
                closePrice = BigDecimal("99.000000000"),
                highestPrice = BigDecimal("99.000000000"),
                lowestPrice = BigDecimal("97.000000000"),
                volume = 100,
                endDateTime = LocalDateTime.parse("2024-01-30T07:16:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            )
        )
    }

    "should get last historic candles in two day" {
        //given
        val now = LocalDateTime.parse("2024-10-07T07:17:00")
        val dateSupplier =
            mockk<DateSupplier> {
                every { currentDateTime() } returns now
            }
        val cacheManager =
            mockk<CacheManager> {
                every { getCache(any()) } returns NoOpCache("cache-name")
            }
        val marketDataService =
            mockk<MarketDataService> {
                every {
                    getCandlesSync(
                        eq("e6123145-9665-43e0-8413-cd61b8aa9b1"),
                        eq(LocalDate.parse("2024-10-07").atStartOfDay().toMskInstant()),
                        eq(now.toMskInstant()),
                        eq(ru.tinkoff.piapi.contract.v1.CandleInterval.CANDLE_INTERVAL_1_MIN)
                    )
                } returns listOf(
                    buildCandle(
                        high = BigDecimal("98"),
                        low = BigDecimal("96"),
                        open = BigDecimal("97"),
                        close = BigDecimal("98"),
                        time = LocalDateTime.parse("2024-10-07T07:12:00")
                    ),
                    buildCandle(
                        high = BigDecimal("99"),
                        low = BigDecimal("97"),
                        open = BigDecimal("98"),
                        close = BigDecimal("99"),
                        time = LocalDateTime.parse("2024-10-07T07:13:00")
                    )
                )
                every {
                    getCandlesSync(
                        eq("e6123145-9665-43e0-8413-cd61b8aa9b1"),
                        eq(LocalDate.parse("2024-10-04").atStartOfDay().toMskInstant()),
                        eq(LocalDate.parse("2024-10-04").atEndOfDay().toMskInstant()),
                        eq(ru.tinkoff.piapi.contract.v1.CandleInterval.CANDLE_INTERVAL_1_MIN)
                    )
                } returns listOf(
                    buildCandle(
                        high = BigDecimal("96"),
                        low = BigDecimal("94"),
                        open = BigDecimal("95"),
                        close = BigDecimal("96"),
                        time = LocalDateTime.parse("2024-10-04T19:00:00")
                    ),
                    buildCandle(
                        high = BigDecimal("97"),
                        low = BigDecimal("95"),
                        open = BigDecimal("96"),
                        close = BigDecimal("97"),
                        time = LocalDateTime.parse("2024-10-04T19:01:00")
                    )
                )
            }
        val adapter = HistoricCandleBrokerOutcomeAdapter(marketDataService, dateSupplier, cacheManager)

        //when
        val candles =
            adapter.getLastHistoricCandles(
                GetLastHistoricCandlesCommand(
                    instrument = Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                    candleInterval = CandleInterval.ONE_MIN,
                    quantity = 4
                )
            )

        //then
        candles shouldBe listOf(
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal("95.000000000"),
                closePrice = BigDecimal("96.000000000"),
                highestPrice = BigDecimal("96.000000000"),
                lowestPrice = BigDecimal("94.000000000"),
                volume = 100,
                endDateTime = LocalDateTime.parse("2024-10-04T19:01:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            ),
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal("96.000000000"),
                closePrice = BigDecimal("97.000000000"),
                highestPrice = BigDecimal("97.000000000"),
                lowestPrice = BigDecimal("95.000000000"),
                volume = 100,
                endDateTime = LocalDateTime.parse("2024-10-04T19:02:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            ),
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal("97.000000000"),
                closePrice = BigDecimal("98.000000000"),
                highestPrice = BigDecimal("98.000000000"),
                lowestPrice = BigDecimal("96.000000000"),
                volume = 100,
                endDateTime = LocalDateTime.parse("2024-10-07T07:13:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            ),
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal("98.000000000"),
                closePrice = BigDecimal("99.000000000"),
                highestPrice = BigDecimal("99.000000000"),
                lowestPrice = BigDecimal("97.000000000"),
                volume = 100,
                endDateTime = LocalDateTime.parse("2024-10-07T07:14:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            )
        )
    }

})