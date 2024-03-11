package ru.kcheranev.trading.test.unit

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.kotest.matchers.types.shouldBeTypeOf

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseBar
import org.ta4j.core.BaseBarSeriesBuilder
import ru.kcheranev.trading.common.DateSupplier
import ru.kcheranev.trading.common.MskDateUtil
import ru.kcheranev.trading.core.strategy.StrategyFactory
import ru.kcheranev.trading.domain.TradeSessionCreatedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionDomainException
import ru.kcheranev.trading.domain.TradeSessionEnteredDomainEvent
import ru.kcheranev.trading.domain.TradeSessionExitedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionExpiredDomainEvent
import ru.kcheranev.trading.domain.TradeSessionPendedForEntryDomainEvent
import ru.kcheranev.trading.domain.TradeSessionPendedForExitDomainEvent
import ru.kcheranev.trading.domain.TradeSessionStoppedDomainEvent
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.TradeStrategy
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime

class TradeSessionTest : StringSpec({

    "should start trade session" {
        //given
        val strategyConfiguration =
            StrategyConfiguration(
                id = StrategyConfigurationId(100),
                type = "strategy-type",
                initCandleAmount = 10,
                candleInterval = CandleInterval.ONE_MIN,
                params = StrategyParameters(mapOf("key" to "value"))
            )
        val strategyFactory = mockk<StrategyFactory>()
        val strategyParamsSlot = slot<StrategyParameters>()
        val seriesSlot = slot<BarSeries>()
        val tradeStrategy = mockk<TradeStrategy>()
        every { strategyFactory.initStrategy(capture(strategyParamsSlot), capture(seriesSlot)) } returns tradeStrategy
        val now = LocalDateTime.parse("2024-01-30T10:15:30")
        val dateSupplier = DateSupplier { now }
        val candle =
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal(101),
                closePrice = BigDecimal(102),
                highestPrice = BigDecimal(102),
                lowestPrice = BigDecimal(100),
                volume = 10,
                endTime = LocalDateTime.parse("2024-01-30T10:19:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            )

        //when
        val tradeSession =
            TradeSession.start(
                strategyConfiguration = strategyConfiguration,
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                lotsQuantity = 10,
                candles = listOf(candle),
                strategyFactory = strategyFactory,
                dateSupplier = dateSupplier
            )

        //then
        tradeSession.id.shouldBeNull()
        tradeSession.ticker shouldBe "SBER"
        tradeSession.instrumentId shouldBe "e6123145-9665-43e0-8413-cd61b8aa9b1"
        tradeSession.status shouldBe TradeSessionStatus.WAITING
        tradeSession.startDate shouldBe now
        tradeSession.candleInterval shouldBe CandleInterval.ONE_MIN
        tradeSession.lotsQuantity shouldBe 10
        tradeSession.strategy shouldBe tradeStrategy
        tradeSession.strategyConfigurationId shouldBe StrategyConfigurationId(100)
        strategyParamsSlot.captured shouldBe StrategyParameters(mapOf("key" to "value"))

        val series = seriesSlot.captured
        series.barCount shouldBe 1
        val expectedBar =
            with(candle) {
                BaseBar(
                    interval.duration,
                    MskDateUtil.toZonedDateTime(endTime),
                    openPrice,
                    highestPrice,
                    lowestPrice,
                    closePrice,
                    BigDecimal(volume)
                )
            }
        series.lastBar shouldBe expectedBar

        tradeSession.events.size shouldBe 1
        val domainEvent = tradeSession.events.first()
        domainEvent.shouldBeTypeOf<TradeSessionCreatedDomainEvent>()
        domainEvent.instrument shouldBe Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
        domainEvent.candleInterval shouldBe CandleInterval.ONE_MIN
    }

    "should pending enter trade session" {
        //given
        val tradeStrategy = mockk<TradeStrategy>()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(1),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(1)
            )
        every { tradeStrategy.series.endIndex } returns 1
        every { tradeStrategy.shouldEnter(1) } returns true

        //when
        tradeSession.executeStrategy()

        //then
        tradeSession.status shouldBe TradeSessionStatus.PENDING_ENTER
        val domainEvent = tradeSession.events.first()
        domainEvent.shouldBeTypeOf<TradeSessionPendedForEntryDomainEvent>()
        domainEvent.tradeSessionId shouldBe TradeSessionId(1)
        domainEvent.instrument shouldBe Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
        domainEvent.candleInterval shouldBe CandleInterval.ONE_MIN
        domainEvent.lotsQuantity shouldBe 10
    }

    "should pending exit trade session" {
        //given
        val tradeStrategy = mockk<TradeStrategy>()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(1),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.IN_POSITION,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(1)
            )
        every { tradeStrategy.series.endIndex } returns 1
        every { tradeStrategy.shouldEnter(1) } returns false
        every { tradeStrategy.shouldExit(1) } returns true

        //when
        tradeSession.executeStrategy()

        //then
        tradeSession.status shouldBe TradeSessionStatus.PENDING_EXIT
        val domainEvent = tradeSession.events.first()
        domainEvent.shouldBeTypeOf<TradeSessionPendedForExitDomainEvent>()
        domainEvent.tradeSessionId shouldBe TradeSessionId(1)
        domainEvent.instrument shouldBe Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
        domainEvent.candleInterval shouldBe CandleInterval.ONE_MIN
        domainEvent.lotsQuantity shouldBe 10
    }

    "should enter trade session" {
        //given
        val tradeStrategy = mockk<TradeStrategy>()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(1),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.PENDING_ENTER,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(1)
            )

        //when
        tradeSession.enter()

        //then
        tradeSession.status shouldBe TradeSessionStatus.IN_POSITION
        val domainEvent = tradeSession.events.first()
        domainEvent.shouldBeTypeOf<TradeSessionEnteredDomainEvent>()
        domainEvent.tradeSessionId shouldBe TradeSessionId(1)
        domainEvent.instrument shouldBe Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
        domainEvent.candleInterval shouldBe CandleInterval.ONE_MIN
    }

    "should exit trade session" {
        //given
        val tradeStrategy = mockk<TradeStrategy>()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(1),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.PENDING_EXIT,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(1)
            )

        //when
        tradeSession.exit()

        //then
        tradeSession.status shouldBe TradeSessionStatus.WAITING
        val domainEvent = tradeSession.events.first()
        domainEvent.shouldBeTypeOf<TradeSessionExitedDomainEvent>()
        domainEvent.tradeSessionId shouldBe TradeSessionId(1)
        domainEvent.instrument shouldBe Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
        domainEvent.candleInterval shouldBe CandleInterval.ONE_MIN
    }

    "should expire trade session" {
        //given
        val tradeStrategy = mockk<TradeStrategy>()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(1),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(1)
            )

        //when
        tradeSession.expire()

        //then
        tradeSession.status shouldBe TradeSessionStatus.EXPIRED
        val domainEvent = tradeSession.events.first()
        domainEvent.shouldBeTypeOf<TradeSessionExpiredDomainEvent>()
        domainEvent.tradeSessionId shouldBe TradeSessionId(1)
        domainEvent.instrument shouldBe Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
        domainEvent.candleInterval shouldBe CandleInterval.ONE_MIN
    }

    "should stop trade session" {
        //given
        val tradeStrategy = mockk<TradeStrategy>()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(1),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(1)
            )

        //when
        tradeSession.stop()

        //then
        tradeSession.status shouldBe TradeSessionStatus.STOPPED
        val domainEvent = tradeSession.events.first()
        domainEvent.shouldBeTypeOf<TradeSessionStoppedDomainEvent>()
        domainEvent.tradeSessionId shouldBe TradeSessionId(1)
        domainEvent.instrument shouldBe Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
        domainEvent.candleInterval shouldBe CandleInterval.ONE_MIN
    }

    "should get last candle date" {
        //given
        val series: BarSeries = BaseBarSeriesBuilder().build()
        series.addBar(
            BaseBar(
                Duration.ofMinutes(1),
                MskDateUtil.toZonedDateTime(LocalDateTime.parse("2024-01-30T10:15:00")),
                BigDecimal(100),
                BigDecimal(102),
                BigDecimal(98),
                BigDecimal(102),
                BigDecimal(10)
            )
        )
        series.addBar(
            BaseBar(
                Duration.ofMinutes(1),
                MskDateUtil.toZonedDateTime(LocalDateTime.parse("2024-01-30T10:16:00")),
                BigDecimal(102),
                BigDecimal(104),
                BigDecimal(100),
                BigDecimal(104),
                BigDecimal(10)
            )
        )
        val tradeStrategy = mockk<TradeStrategy>()
        every { tradeStrategy.series } returns series
        val tradeSession =
            TradeSession(
                id = TradeSessionId(1),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(1)
            )

        //when
        val lastCandleDate = tradeSession.lastCandleDate()

        //then
        lastCandleDate shouldBe LocalDateTime.parse("2024-01-30T10:16:00")
    }

    listOf<Long>(5, 6).forEach { availableCandleDelay ->
        "should check candle series is fresh when availableCandleDelay is $availableCandleDelay" {
            val series: BarSeries = BaseBarSeriesBuilder().build()
            series.addBar(
                BaseBar(
                    Duration.ofMinutes(1),
                    MskDateUtil.toZonedDateTime(LocalDateTime.parse("2024-01-30T10:15:00")),
                    BigDecimal(100),
                    BigDecimal(102),
                    BigDecimal(98),
                    BigDecimal(102),
                    BigDecimal(10)
                )
            )
            val tradeStrategy = mockk<TradeStrategy>()
            every { tradeStrategy.series } returns series
            val dateSupplier = DateSupplier { LocalDateTime.parse("2024-01-30T10:20:30") }
            val tradeSession =
                TradeSession(
                    id = TradeSessionId(1),
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    status = TradeSessionStatus.WAITING,
                    startDate = LocalDateTime.now(),
                    candleInterval = CandleInterval.ONE_MIN,
                    lotsQuantity = 10,
                    strategy = tradeStrategy,
                    strategyConfigurationId = StrategyConfigurationId(1)
                )

            //when
            val freshCandleSeries = tradeSession.freshCandleSeries(availableCandleDelay, dateSupplier)

            //then
            freshCandleSeries.shouldBeTrue()
        }
    }

    "should check candle series is not fresh" {
        val series: BarSeries = BaseBarSeriesBuilder().build()
        series.addBar(
            BaseBar(
                Duration.ofMinutes(1),
                MskDateUtil.toZonedDateTime(LocalDateTime.parse("2024-01-30T10:15:00")),
                BigDecimal(100),
                BigDecimal(102),
                BigDecimal(98),
                BigDecimal(102),
                BigDecimal(10)
            )
        )
        val tradeStrategy = mockk<TradeStrategy>()
        every { tradeStrategy.series } returns series
        val dateSupplier = DateSupplier { LocalDateTime.parse("2024-01-30T10:20:30") }
        val tradeSession =
            TradeSession(
                id = TradeSessionId(1),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(1)
            )

        //when
        val freshCandleSeries = tradeSession.freshCandleSeries(4, dateSupplier)

        //then
        freshCandleSeries.shouldBeFalse()
    }

    listOf<Long>(3, 4).forEach { maxCandleDelay ->
        "should check candle series is expired when maxCandleDelay is $maxCandleDelay" {
            val series: BarSeries = BaseBarSeriesBuilder().build()
            series.addBar(
                BaseBar(
                    Duration.ofMinutes(1),
                    MskDateUtil.toZonedDateTime(LocalDateTime.parse("2024-01-30T10:15:00")),
                    BigDecimal(100),
                    BigDecimal(102),
                    BigDecimal(98),
                    BigDecimal(102),
                    BigDecimal(10)
                )
            )
            val tradeStrategy = mockk<TradeStrategy>()
            every { tradeStrategy.series } returns series
            val dateSupplier = DateSupplier { LocalDateTime.parse("2024-01-30T10:20:30") }
            val tradeSession =
                TradeSession(
                    id = TradeSessionId(1),
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    status = TradeSessionStatus.WAITING,
                    startDate = LocalDateTime.now(),
                    candleInterval = CandleInterval.ONE_MIN,
                    lotsQuantity = 10,
                    strategy = tradeStrategy,
                    strategyConfigurationId = StrategyConfigurationId(1)
                )

            //when
            val expiredCandleSeries = tradeSession.expiredCandleSeries(maxCandleDelay, dateSupplier)

            //then
            expiredCandleSeries.shouldBeTrue()
        }
    }

    "should check candle series is not expired" {
        val series: BarSeries = BaseBarSeriesBuilder().build()
        series.addBar(
            BaseBar(
                Duration.ofMinutes(1),
                MskDateUtil.toZonedDateTime(LocalDateTime.parse("2024-01-30T10:15:00")),
                BigDecimal(100),
                BigDecimal(102),
                BigDecimal(98),
                BigDecimal(102),
                BigDecimal(10)
            )
        )
        val tradeStrategy = mockk<TradeStrategy>()
        every { tradeStrategy.series } returns series
        val dateSupplier = DateSupplier { LocalDateTime.parse("2024-01-30T10:20:30") }
        val tradeSession =
            TradeSession(
                id = TradeSessionId(1),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(1)
            )

        //when
        val expiredCandleSeries = tradeSession.expiredCandleSeries(5, dateSupplier)

        //then
        expiredCandleSeries.shouldBeFalse()
    }

    "should add candle to series" {
        //given
        val mockedSeries: BarSeries = BaseBarSeriesBuilder().build()
        mockedSeries.addBar(
            BaseBar(
                Duration.ofMinutes(1),
                MskDateUtil.toZonedDateTime(LocalDateTime.parse("2024-01-30T10:15:00")),
                BigDecimal(100),
                BigDecimal(102),
                BigDecimal(98),
                BigDecimal(102),
                BigDecimal(10)
            )
        )
        val tradeStrategy = mockk<TradeStrategy> {
            every { series } returns mockedSeries
            every { addBar(any()) } answers { callOriginal() }
        }
        val candle =
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal(102),
                closePrice = BigDecimal(104),
                highestPrice = BigDecimal(104),
                lowestPrice = BigDecimal(97),
                volume = 10,
                endTime = LocalDateTime.parse("2024-01-30T10:19:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            )
        val tradeSession =
            TradeSession(
                id = TradeSessionId(1),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(1)
            )

        //when
        tradeSession.addCandle(candle, 5)

        //then
        mockedSeries.barCount shouldBe 2
        val lastBar = mockedSeries.lastBar
        lastBar.endTime shouldBe MskDateUtil.toZonedDateTime(LocalDateTime.parse("2024-01-30T10:19:00"))
    }

    "should throw TradeSessionDomainException while adding new candle when new candle date intersects series dates" {
        //given
        val mockedSeries: BarSeries = BaseBarSeriesBuilder().build()
        mockedSeries.addBar(
            BaseBar(
                Duration.ofMinutes(1),
                MskDateUtil.toZonedDateTime(LocalDateTime.parse("2024-01-30T10:15:00")),
                BigDecimal(100),
                BigDecimal(102),
                BigDecimal(98),
                BigDecimal(102),
                BigDecimal(10)
            )
        )
        val tradeStrategy = mockk<TradeStrategy> {
            every { series } returns mockedSeries
            every { addBar(any()) } answers { callOriginal() }
        }
        val candle =
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal(102),
                closePrice = BigDecimal(104),
                highestPrice = BigDecimal(104),
                lowestPrice = BigDecimal(97),
                volume = 10,
                endTime = LocalDateTime.parse("2024-01-30T10:14:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            )
        val tradeSession =
            TradeSession(
                id = TradeSessionId(1),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(1)
            )

        //when
        val ex = shouldThrow<TradeSessionDomainException> { tradeSession.addCandle(candle, 5) }

        //then
        ex shouldHaveMessage "Unable to add candle to the trade session 1 strategy series: new candle date intersects series dates"
    }

    "should throw TradeSessionDomainException while adding new candle when strategy series is delayed" {
        //given
        val mockedSeries: BarSeries = BaseBarSeriesBuilder().build()
        mockedSeries.addBar(
            BaseBar(
                Duration.ofMinutes(1),
                MskDateUtil.toZonedDateTime(LocalDateTime.parse("2024-01-30T10:15:00")),
                BigDecimal(100),
                BigDecimal(102),
                BigDecimal(98),
                BigDecimal(102),
                BigDecimal(10)
            )
        )
        val tradeStrategy = mockk<TradeStrategy> {
            every { series } returns mockedSeries
            every { addBar(any()) } answers { callOriginal() }
        }
        val candle =
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal(102),
                closePrice = BigDecimal(104),
                highestPrice = BigDecimal(104),
                lowestPrice = BigDecimal(97),
                volume = 10,
                endTime = LocalDateTime.parse("2024-01-30T10:21:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            )
        val tradeSession =
            TradeSession(
                id = TradeSessionId(1),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(1)
            )

        //when
        val ex = shouldThrow<TradeSessionDomainException> { tradeSession.addCandle(candle, 5) }

        //then
        ex shouldHaveMessage "Unable to add candle to the trade session 1 strategy series: series is delayed"
    }

    "should refresh candle series" {
        //given
        val mockedSeries: BarSeries = BaseBarSeriesBuilder().build()
        mockedSeries.addBar(
            BaseBar(
                Duration.ofMinutes(1),
                MskDateUtil.toZonedDateTime(LocalDateTime.parse("2024-01-30T10:15:00")),
                BigDecimal(100),
                BigDecimal(102),
                BigDecimal(98),
                BigDecimal(102),
                BigDecimal(10)
            )
        )
        val tradeStrategy = mockk<TradeStrategy> {
            every { series } returns mockedSeries
            every { addBar(any()) } answers { callOriginal() }
        }
        val candle1 =
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal(102),
                closePrice = BigDecimal(104),
                highestPrice = BigDecimal(104),
                lowestPrice = BigDecimal(97),
                volume = 10,
                endTime = LocalDateTime.parse("2024-01-30T10:16:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            )
        val candle2 =
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal(104),
                closePrice = BigDecimal(106),
                highestPrice = BigDecimal(106),
                lowestPrice = BigDecimal(101),
                volume = 10,
                endTime = LocalDateTime.parse("2024-01-30T10:17:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            )
        val tradeSession =
            TradeSession(
                id = TradeSessionId(1),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(1)
            )

        //when
        tradeSession.refreshCandleSeries(listOf(candle1, candle2))

        //then
        mockedSeries.barCount shouldBe 3
        mockedSeries.getBar(1).endTime shouldBe MskDateUtil.toZonedDateTime(LocalDateTime.parse("2024-01-30T10:16:00"))
        mockedSeries.getBar(2).endTime shouldBe MskDateUtil.toZonedDateTime(LocalDateTime.parse("2024-01-30T10:17:00"))
    }

    "should throw TradeSessionDomainException while refreshing candle series when new candle dates intersect series dates" {
        //given
        val mockedSeries: BarSeries = BaseBarSeriesBuilder().build()
        mockedSeries.addBar(
            BaseBar(
                Duration.ofMinutes(1),
                MskDateUtil.toZonedDateTime(LocalDateTime.parse("2024-01-30T10:15:00")),
                BigDecimal(100),
                BigDecimal(102),
                BigDecimal(98),
                BigDecimal(102),
                BigDecimal(10)
            )
        )
        val tradeStrategy = mockk<TradeStrategy> {
            every { series } returns mockedSeries
            every { addBar(any()) } answers { callOriginal() }
        }
        val candle =
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal(102),
                closePrice = BigDecimal(104),
                highestPrice = BigDecimal(104),
                lowestPrice = BigDecimal(97),
                volume = 10,
                endTime = LocalDateTime.parse("2024-01-30T10:14:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            )
        val tradeSession =
            TradeSession(
                id = TradeSessionId(1),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(1)
            )

        //when
        val ex = shouldThrow<TradeSessionDomainException> { tradeSession.refreshCandleSeries(listOf(candle)) }

        //then
        ex shouldHaveMessage "Unable to refresh trade session 1 strategy series: new candles dates intersect series dates"
    }

})