package com.github.trading.test.unit.adapter

import com.github.trading.common.date.DateSupplier
import com.github.trading.common.date.atEndOfDay
import com.github.trading.common.date.toMskInstant
import com.github.trading.core.port.outcome.broker.GetLastHistoricCandlesCommand
import com.github.trading.domain.model.Candle
import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.Instrument
import com.github.trading.infra.adapter.outcome.broker.impl.HistoricCandleBrokerOutcomeAdapter
import com.github.trading.infra.util.instantToTimestamp
import com.github.trading.test.extension.MockDateSupplierExtension
import com.google.protobuf.Timestamp
import io.grpc.stub.AbstractBlockingStub
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.cache.CacheManager
import org.springframework.cache.support.NoOpCache
import ru.tinkoff.piapi.contract.v1.GetCandlesRequest
import ru.tinkoff.piapi.contract.v1.GetCandlesRequest.CandleSource
import ru.tinkoff.piapi.contract.v1.GetCandlesResponse
import ru.tinkoff.piapi.contract.v1.HistoricCandle
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc.MarketDataServiceBlockingStub
import ru.ttech.piapi.core.connector.SyncStubWrapper
import ru.ttech.piapi.core.helpers.NumberMapper.bigDecimalToQuotation
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class HistoricCandleBrokerOutcomeAdapterTest : StringSpec({

    extensions(MockDateSupplierExtension())

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

    fun <T : AbstractBlockingStub<T>> buildSyncStubWrapper(stub: T): SyncStubWrapper<T> {
        val constructor = SyncStubWrapper::class.java.getDeclaredConstructor(AbstractBlockingStub::class.java)
        constructor.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return constructor.newInstance(stub) as SyncStubWrapper<T>
    }

    "should get last historic candles in one day" {
        //given
        val now = LocalDateTime.parse("2024-01-30T07:17:00")
        every { DateSupplier.currentDateTime() } returns now
        val cacheManager =
            mockk<CacheManager> {
                every { getCache(any()) } returns NoOpCache("cache-name")
            }
        val marketDataService =
            mockk<MarketDataServiceBlockingStub> {
                every {
                    getCandles(
                        eq(
                            GetCandlesRequest.newBuilder()
                                .setInstrumentId("e6123145-9665-43e0-8413-cd61b8aa9b1")
                                .setFrom(instantToTimestamp(LocalDate.parse("2024-01-30").atStartOfDay().toMskInstant()))
                                .setTo(instantToTimestamp(now.toMskInstant()))
                                .setInterval(ru.tinkoff.piapi.contract.v1.CandleInterval.CANDLE_INTERVAL_1_MIN)
                                .setCandleSourceType(CandleSource.CANDLE_SOURCE_EXCHANGE)
                                .build()
                        )
                    )
                } returns
                        GetCandlesResponse.newBuilder()
                            .addAllCandles(
                                listOf(
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
                            ).build()
            }
        val adapter = HistoricCandleBrokerOutcomeAdapter(buildSyncStubWrapper(marketDataService), cacheManager)

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
        every { DateSupplier.currentDateTime() } returns now
        val cacheManager =
            mockk<CacheManager> {
                every { getCache(any()) } returns NoOpCache("cache-name")
            }
        val marketDataService =
            mockk<MarketDataServiceBlockingStub> {
                every {
                    getCandles(
                        eq(
                            GetCandlesRequest.newBuilder()
                                .setInstrumentId("e6123145-9665-43e0-8413-cd61b8aa9b1")
                                .setFrom(instantToTimestamp(LocalDate.parse("2024-10-07").atStartOfDay().toMskInstant()))
                                .setTo(instantToTimestamp(now.toMskInstant()))
                                .setInterval(ru.tinkoff.piapi.contract.v1.CandleInterval.CANDLE_INTERVAL_1_MIN)
                                .setCandleSourceType(CandleSource.CANDLE_SOURCE_EXCHANGE)
                                .build()
                        )
                    )
                } returns
                        GetCandlesResponse.newBuilder()
                            .addAllCandles(
                                listOf(
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
                            ).build()
                every {
                    getCandles(
                        eq(
                            GetCandlesRequest.newBuilder()
                                .setInstrumentId("e6123145-9665-43e0-8413-cd61b8aa9b1")
                                .setFrom(instantToTimestamp(LocalDate.parse("2024-10-06").atStartOfDay().toMskInstant()))
                                .setTo(instantToTimestamp(LocalDate.parse("2024-10-06").atEndOfDay().toMskInstant()))
                                .setInterval(ru.tinkoff.piapi.contract.v1.CandleInterval.CANDLE_INTERVAL_1_MIN)
                                .setCandleSourceType(CandleSource.CANDLE_SOURCE_EXCHANGE)
                                .build()
                        )
                    )
                } returns
                        GetCandlesResponse.newBuilder()
                            .addAllCandles(
                                listOf(
                                    buildCandle(
                                        high = BigDecimal("96"),
                                        low = BigDecimal("94"),
                                        open = BigDecimal("95"),
                                        close = BigDecimal("96"),
                                        time = LocalDateTime.parse("2024-10-06T19:00:00")
                                    ),
                                    buildCandle(
                                        high = BigDecimal("97"),
                                        low = BigDecimal("95"),
                                        open = BigDecimal("96"),
                                        close = BigDecimal("97"),
                                        time = LocalDateTime.parse("2024-10-06T19:01:00")
                                    )
                                )
                            ).build()
            }
        val adapter = HistoricCandleBrokerOutcomeAdapter(buildSyncStubWrapper(marketDataService), cacheManager)

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
                endDateTime = LocalDateTime.parse("2024-10-06T19:01:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            ),
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal("96.000000000"),
                closePrice = BigDecimal("97.000000000"),
                highestPrice = BigDecimal("97.000000000"),
                lowestPrice = BigDecimal("95.000000000"),
                volume = 100,
                endDateTime = LocalDateTime.parse("2024-10-06T19:02:00"),
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