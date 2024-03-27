package ru.kcheranev.trading.test.unit.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.collections.shouldHaveSize
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
import ru.kcheranev.trading.domain.TradeSessionMovedToWaitingForEntryDomainEvent
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
import java.util.UUID

class TradeSessionTest : StringSpec({

    "should start trade session" {
        //given
        val strategyConfigurationId = UUID.fromString("d18bbe01-6e7a-44dd-a4cf-7fcc0c2ac874")
        val strategyConfiguration =
            StrategyConfiguration(
                id = StrategyConfigurationId(strategyConfigurationId),
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
        tradeSession.strategyConfigurationId shouldBe StrategyConfigurationId(strategyConfigurationId)
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
            every { shouldEnter(1) } returns false
            every { shouldExit(1) } returns false
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
                id = TradeSessionId(UUID.randomUUID()),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(UUID.randomUUID())
            )

        //when
        tradeSession.processIncomeCandle(candle, 5)

        //then
        mockedSeries.barCount shouldBe 2
        val lastBar = mockedSeries.lastBar
        lastBar.endTime shouldBe MskDateUtil.toZonedDateTime(LocalDateTime.parse("2024-01-30T10:19:00"))
    }

    "should pending enter trade session" {
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
            every { shouldEnter(1) } returns true
            every { addBar(any()) } answers { callOriginal() }
        }
        val tradeSessionId = UUID.randomUUID()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(tradeSessionId),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(UUID.randomUUID())
            )
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

        //when
        tradeSession.processIncomeCandle(candle, 5)

        //then
        tradeSession.status shouldBe TradeSessionStatus.PENDING_ENTER
        val domainEvents = tradeSession.events
        domainEvents shouldHaveSize 2
        val pendingEnterEvent = domainEvents[1]
        pendingEnterEvent.shouldBeTypeOf<TradeSessionPendedForEntryDomainEvent>()
        pendingEnterEvent.tradeSessionId shouldBe TradeSessionId(tradeSessionId)
        pendingEnterEvent.instrument shouldBe Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
        pendingEnterEvent.candleInterval shouldBe CandleInterval.ONE_MIN
        pendingEnterEvent.lotsQuantity shouldBe 10
    }

    "should pending exit trade session" {
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
            every { shouldEnter(1) } returns false
            every { shouldExit(1) } returns true
            every { addBar(any()) } answers { callOriginal() }
        }
        val tradeSessionId = UUID.randomUUID()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(tradeSessionId),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.IN_POSITION,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                lotsQuantityInPosition = 5,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(UUID.randomUUID())
            )
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

        //when
        tradeSession.processIncomeCandle(candle, 5)

        //then
        tradeSession.status shouldBe TradeSessionStatus.PENDING_EXIT
        val domainEvents = tradeSession.events
        domainEvents shouldHaveSize 2
        val pendingExitEvent = domainEvents[1]
        pendingExitEvent.shouldBeTypeOf<TradeSessionPendedForExitDomainEvent>()
        pendingExitEvent.tradeSessionId shouldBe TradeSessionId(tradeSessionId)
        pendingExitEvent.instrument shouldBe Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
        pendingExitEvent.candleInterval shouldBe CandleInterval.ONE_MIN
        pendingExitEvent.lotsQuantityInPosition shouldBe 5
    }

    "should enter trade session" {
        //given
        val tradeStrategy = mockk<TradeStrategy>()
        val tradeSessionId = UUID.randomUUID()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(tradeSessionId),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.PENDING_ENTER,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(UUID.randomUUID())
            )

        //when
        tradeSession.enter(5)

        //then
        tradeSession.status shouldBe TradeSessionStatus.IN_POSITION
        tradeSession.lotsQuantityInPosition shouldBe 5
        val domainEvents = tradeSession.events
        domainEvents shouldHaveSize 1
        val enteredEvent = domainEvents.first()
        enteredEvent.shouldBeTypeOf<TradeSessionEnteredDomainEvent>()
        enteredEvent.tradeSessionId shouldBe TradeSessionId(tradeSessionId)
        enteredEvent.instrument shouldBe Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
        enteredEvent.candleInterval shouldBe CandleInterval.ONE_MIN
    }

    "should exit trade session" {
        //given
        val tradeStrategy = mockk<TradeStrategy>()
        val tradeSessionId = UUID.randomUUID()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(tradeSessionId),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.PENDING_EXIT,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(UUID.randomUUID())
            )

        //when
        tradeSession.exit(5)

        //then
        tradeSession.status shouldBe TradeSessionStatus.WAITING
        tradeSession.lotsQuantityInPosition shouldBe 0
        val domainEvents = tradeSession.events
        domainEvents shouldHaveSize 1
        val exitedEvent = domainEvents.first()
        exitedEvent.shouldBeTypeOf<TradeSessionExitedDomainEvent>()
        exitedEvent.tradeSessionId shouldBe TradeSessionId(tradeSessionId)
        exitedEvent.instrument shouldBe Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
        exitedEvent.candleInterval shouldBe CandleInterval.ONE_MIN
    }

    "should expire trade session" {
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
        }
        val tradeSessionId = UUID.randomUUID()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(tradeSessionId),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(UUID.randomUUID())
            )
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

        //when
        tradeSession.processIncomeCandle(candle, 5)

        //then
        tradeSession.status shouldBe TradeSessionStatus.EXPIRED
        val domainEvents = tradeSession.events
        domainEvents shouldHaveSize 1
        val expiredEvent = domainEvents.first()
        expiredEvent.shouldBeTypeOf<TradeSessionExpiredDomainEvent>()
        expiredEvent.tradeSessionId shouldBe TradeSessionId(tradeSessionId)
        expiredEvent.instrument shouldBe Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
        expiredEvent.candleInterval shouldBe CandleInterval.ONE_MIN
    }

    "should stop trade session" {
        //given
        val tradeStrategy = mockk<TradeStrategy>()
        val tradeSessionId = UUID.randomUUID()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(tradeSessionId),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(UUID.randomUUID())
            )

        //when
        tradeSession.stop()

        //then
        tradeSession.status shouldBe TradeSessionStatus.STOPPED
        val domainEvents = tradeSession.events
        domainEvents shouldHaveSize 1
        val stoppedEvent = domainEvents.first()
        stoppedEvent.shouldBeTypeOf<TradeSessionStoppedDomainEvent>()
        stoppedEvent.tradeSessionId shouldBe TradeSessionId(tradeSessionId)
        stoppedEvent.instrument shouldBe Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
        stoppedEvent.candleInterval shouldBe CandleInterval.ONE_MIN
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
        val tradeSessionId = UUID.randomUUID()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(tradeSessionId),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.now(),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyConfigurationId = StrategyConfigurationId(UUID.randomUUID())
            )

        //when
        val ex = shouldThrow<TradeSessionDomainException> { tradeSession.processIncomeCandle(candle, 5) }

        //then
        ex shouldHaveMessage "Unable to process income candle: new candle date intersects trade session " +
                "[id=$tradeSessionId, ticker=SBER, instrumentId=e6123145-9665-43e0-8413-cd61b8aa9b1, " +
                "status=WAITING, candleInterval=ONE_MIN] series dates"
    }

    "should waiting for entry" {
        forAll(
            row(TradeSessionStatus.PENDING_ENTER, TradeSessionStatus.WAITING),
            row(TradeSessionStatus.PENDING_EXIT, TradeSessionStatus.IN_POSITION),
        ) { currentStatus, targetStatus ->
            //given
            val tradeStrategy = mockk<TradeStrategy>()
            val tradeSessionId = UUID.randomUUID()
            val tradeSession =
                TradeSession(
                    id = TradeSessionId(tradeSessionId),
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    status = currentStatus,
                    startDate = LocalDateTime.now(),
                    candleInterval = CandleInterval.ONE_MIN,
                    lotsQuantity = 10,
                    strategy = tradeStrategy,
                    strategyConfigurationId = StrategyConfigurationId(UUID.randomUUID())
                )

            //when
            tradeSession.waitForEntry()

            //then
            tradeSession.status shouldBe targetStatus
            val domainEvents = tradeSession.events
            domainEvents shouldHaveSize 1
            val stoppedEvent = domainEvents.first()
            stoppedEvent.shouldBeTypeOf<TradeSessionMovedToWaitingForEntryDomainEvent>()
            stoppedEvent.tradeSessionId shouldBe TradeSessionId(tradeSessionId)
        }
    }

})