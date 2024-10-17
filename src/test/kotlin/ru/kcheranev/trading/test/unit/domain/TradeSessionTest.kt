package ru.kcheranev.trading.test.unit.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import org.ta4j.core.BaseBar
import org.ta4j.core.BaseBarSeriesBuilder
import org.ta4j.core.Strategy
import ru.kcheranev.trading.common.date.toMskZonedDateTime
import ru.kcheranev.trading.core.config.TradingProperties
import ru.kcheranev.trading.core.config.TradingScheduleInterval
import ru.kcheranev.trading.domain.TradeSessionCreatedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionDomainException
import ru.kcheranev.trading.domain.TradeSessionEnteredDomainEvent
import ru.kcheranev.trading.domain.TradeSessionExitedDomainEvent
import ru.kcheranev.trading.domain.TradeSessionPendedForEntryDomainEvent
import ru.kcheranev.trading.domain.TradeSessionPendedForExitDomainEvent
import ru.kcheranev.trading.domain.TradeSessionResumedDomainEvent
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
import java.time.LocalTime
import java.util.UUID

class TradeSessionTest : FreeSpec({

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

    "should start trade session" {
        //given
        val strategyConfigurationId = UUID.fromString("d18bbe01-6e7a-44dd-a4cf-7fcc0c2ac874")
        val strategyConfiguration =
            StrategyConfiguration(
                id = StrategyConfigurationId(strategyConfigurationId),
                name = "dummy",
                type = "strategy-type",
                candleInterval = CandleInterval.ONE_MIN,
                parameters = StrategyParameters(mapOf("key" to 1))
            )
        val tradeStrategy = mockk<TradeStrategy>()

        //when
        val tradeSession =
            TradeSession.create(
                strategyConfiguration = strategyConfiguration,
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                lotsQuantity = 10,
                tradeStrategy = tradeStrategy
            )

        //then
        tradeSession.id.shouldNotBeNull()
        tradeSession.ticker shouldBe "SBER"
        tradeSession.instrumentId shouldBe "e6123145-9665-43e0-8413-cd61b8aa9b1"
        tradeSession.status shouldBe TradeSessionStatus.WAITING
        tradeSession.candleInterval shouldBe CandleInterval.ONE_MIN
        tradeSession.lotsQuantity shouldBe 10
        tradeSession.strategy shouldBe tradeStrategy
        tradeSession.strategyType shouldBe "strategy-type"
        tradeSession.strategyParameters shouldBe StrategyParameters(mapOf("key" to 1))

        tradeSession.events.size shouldBe 1
        val domainEvent = tradeSession.events.first()
        domainEvent.shouldBeTypeOf<TradeSessionCreatedDomainEvent>()
        domainEvent.instrument shouldBe Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
        domainEvent.candleInterval shouldBe CandleInterval.ONE_MIN
    }

    "should add candle to series" {
        //given
        val barSeries =
            BaseBarSeriesBuilder().build()
                .apply {
                    addBar(
                        BaseBar(
                            Duration.ofMinutes(1),
                            LocalDateTime.parse("2024-01-30T10:15:00").toMskZonedDateTime(),
                            BigDecimal(100),
                            BigDecimal(102),
                            BigDecimal(98),
                            BigDecimal(102),
                            BigDecimal(10)
                        )
                    )
                }
        val tradeStrategy =
            spyk(TradeStrategy(barSeries, false, mockk<Strategy>())) {
                every { shouldEnter(any()) } returns false
                every { shouldExit(any()) } returns false
            }
        val candle =
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal(102),
                closePrice = BigDecimal(104),
                highestPrice = BigDecimal(104),
                lowestPrice = BigDecimal(97),
                volume = 10,
                endDateTime = LocalDateTime.parse("2024-01-30T10:19:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            )
        val tradeSession =
            TradeSession(
                id = TradeSessionId(UUID.randomUUID()),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyType = "DUMMY",
                strategyParameters = StrategyParameters(mapOf("paramName" to 1))
            )

        //when
        tradeSession.processIncomeCandle(candle)

        //then
        barSeries.barCount shouldBe 2
        val lastBar = barSeries.lastBar
        lastBar.endTime shouldBe LocalDateTime.parse("2024-01-30T10:19:00").toMskZonedDateTime()
    }

    "should pending enter trade session" {
        //given
        val barSeries =
            BaseBarSeriesBuilder().build()
                .apply {
                    addBar(
                        BaseBar(
                            Duration.ofMinutes(1),
                            LocalDateTime.parse("2024-01-30T10:15:00").toMskZonedDateTime(),
                            BigDecimal(100),
                            BigDecimal(102),
                            BigDecimal(98),
                            BigDecimal(102),
                            BigDecimal(10)
                        )
                    )
                }
        val tradeStrategy =
            spyk(TradeStrategy(barSeries, false, mockk<Strategy>())) {
                every { shouldEnter(any()) } returns true
            }
        val tradeSessionId = UUID.randomUUID()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(tradeSessionId),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyType = "DUMMY",
                strategyParameters = StrategyParameters(mapOf("paramName" to 1))
            )
        val candle =
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal(102),
                closePrice = BigDecimal(104),
                highestPrice = BigDecimal(104),
                lowestPrice = BigDecimal(97),
                volume = 10,
                endDateTime = LocalDateTime.parse("2024-01-30T10:19:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            )

        //when
        tradeSession.processIncomeCandle(candle)

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
        val barSeries =
            BaseBarSeriesBuilder().build()
                .apply {
                    addBar(
                        BaseBar(
                            Duration.ofMinutes(1),
                            LocalDateTime.parse("2024-01-30T10:15:00").toMskZonedDateTime(),
                            BigDecimal(100),
                            BigDecimal(102),
                            BigDecimal(98),
                            BigDecimal(102),
                            BigDecimal(10)
                        )
                    )
                }
        val tradeStrategy =
            spyk(TradeStrategy(barSeries, false, mockk<Strategy>())) {
                every { shouldEnter(any()) } returns true
                every { shouldExit(any()) } returns true
            }
        val tradeSessionId = UUID.randomUUID()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(tradeSessionId),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.IN_POSITION,
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                lotsQuantityInPosition = 5,
                strategy = tradeStrategy,
                strategyType = "DUMMY",
                strategyParameters = StrategyParameters(mapOf("paramName" to 1))
            )
        val candle =
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal(102),
                closePrice = BigDecimal(104),
                highestPrice = BigDecimal(104),
                lowestPrice = BigDecimal(97),
                volume = 10,
                endDateTime = LocalDateTime.parse("2024-01-30T10:19:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            )

        //when
        tradeSession.processIncomeCandle(candle)

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
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyType = "DUMMY",
                strategyParameters = StrategyParameters(mapOf("paramName" to 1))
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
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyType = "DUMMY",
                strategyParameters = StrategyParameters(mapOf("paramName" to 1))
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

    "should resume trade session" - {
        data class TestParameters(
            val currentStatus: TradeSessionStatus,
            val targetStatus: TradeSessionStatus,
            val lotsQuantityInPosition: Int
        )
        withData(
            nameFn = { "current = ${it.currentStatus}, target = ${it.targetStatus}, is in position = ${it.lotsQuantityInPosition > 0}" },
            TestParameters(TradeSessionStatus.PENDING_ENTER, TradeSessionStatus.WAITING, 0),
            TestParameters(TradeSessionStatus.PENDING_EXIT, TradeSessionStatus.IN_POSITION, 10),
            TestParameters(TradeSessionStatus.STOPPED, TradeSessionStatus.IN_POSITION, 10),
            TestParameters(TradeSessionStatus.STOPPED, TradeSessionStatus.WAITING, 0)
        ) { (currentStatus, targetStatus, lotsQuantityInPosition) ->
            //given
            val tradeStrategy = mockk<TradeStrategy>()
            val tradeSessionId = UUID.randomUUID()
            val tradeSession =
                TradeSession(
                    id = TradeSessionId(tradeSessionId),
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    status = currentStatus,
                    candleInterval = CandleInterval.ONE_MIN,
                    lotsQuantity = 10,
                    lotsQuantityInPosition = lotsQuantityInPosition,
                    strategy = tradeStrategy,
                    strategyType = "DUMMY",
                    strategyParameters = StrategyParameters(mapOf("paramName" to 1))
                )

            //when
            tradeSession.resume()

            //then
            tradeSession.status shouldBe targetStatus
            val domainEvents = tradeSession.events
            domainEvents shouldHaveSize 1
            val stoppedEvent = domainEvents.first()
            stoppedEvent.shouldBeTypeOf<TradeSessionResumedDomainEvent>()
            stoppedEvent.tradeSessionId shouldBe TradeSessionId(tradeSessionId)
        }
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
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyType = "DUMMY",
                strategyParameters = StrategyParameters(mapOf("paramName" to 1))
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
        val barSeries =
            BaseBarSeriesBuilder().build()
                .apply {
                    addBar(
                        BaseBar(
                            Duration.ofMinutes(1),
                            LocalDateTime.parse("2024-01-30T10:15:00").toMskZonedDateTime(),
                            BigDecimal(100),
                            BigDecimal(102),
                            BigDecimal(98),
                            BigDecimal(102),
                            BigDecimal(10)
                        )
                    )
                }
        val tradeStrategy = TradeStrategy(barSeries, false, mockk<Strategy>())
        val candle =
            Candle(
                interval = CandleInterval.ONE_MIN,
                openPrice = BigDecimal(102),
                closePrice = BigDecimal(104),
                highestPrice = BigDecimal(104),
                lowestPrice = BigDecimal(97),
                volume = 10,
                endDateTime = LocalDateTime.parse("2024-01-30T10:14:00"),
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
            )
        val tradeSessionId = UUID.randomUUID()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(tradeSessionId),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = tradeStrategy,
                strategyType = "DUMMY",
                strategyParameters = StrategyParameters(mapOf("paramName" to 1))
            )

        //when
        val ex = shouldThrow<TradeSessionDomainException> { tradeSession.processIncomeCandle(candle) }

        //then
        ex shouldHaveMessage "Unable to process income candle: new candle date intersects trade session " +
                "[id=$tradeSessionId, ticker=SBER, instrumentId=e6123145-9665-43e0-8413-cd61b8aa9b1, " +
                "status=WAITING, candleInterval=ONE_MIN] series dates"
    }

})