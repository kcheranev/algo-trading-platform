package com.github.trading.test.unit.domain

import arrow.core.right
import com.github.trading.common.date.toMskInstant
import com.github.trading.core.config.TradingProperties
import com.github.trading.core.strategy.lotsquantity.LOTS_QUANTITY_STRATEGY_PARAMETER_NAME
import com.github.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategy
import com.github.trading.domain.TradeSessionCreatedDomainEvent
import com.github.trading.domain.TradeSessionEnteredDomainEvent
import com.github.trading.domain.TradeSessionExitedDomainEvent
import com.github.trading.domain.TradeSessionPendedForEntryDomainEvent
import com.github.trading.domain.TradeSessionPendedForExitDomainEvent
import com.github.trading.domain.TradeSessionResumedDomainEvent
import com.github.trading.domain.TradeSessionStoppedDomainEvent
import com.github.trading.domain.entity.CurrentPosition
import com.github.trading.domain.entity.StrategyConfiguration
import com.github.trading.domain.entity.StrategyConfigurationId
import com.github.trading.domain.entity.TradeSession
import com.github.trading.domain.entity.TradeSessionId
import com.github.trading.domain.entity.TradeSessionStatus
import com.github.trading.domain.exception.TradeSessionDomainException
import com.github.trading.domain.model.Candle
import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.Position
import com.github.trading.domain.model.StrategyParameters
import com.github.trading.domain.model.TradeStrategy
import com.github.trading.test.extension.MockDateSupplierExtension
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkObject
import org.ta4j.core.BaseBarSeriesBuilder
import org.ta4j.core.Strategy
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

class TradeSessionTest : FreeSpec({

    extensions(MockDateSupplierExtension())

    val orderLotsQuantityStrategy =
        mockk<OrderLotsQuantityStrategy> {
            every { getLotsQuantity(any()) } returns 10.right()
        }

    beforeSpec {
        mockkObject(TradingProperties.Companion)
        every { TradingProperties.tradingProperties } returns
                TradingProperties(
                    availableDelayedCandlesCount = 5,
                    placeOrderRetryCount = 3,
                    defaultCommission = BigDecimal("0.0004")
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
                parameters = StrategyParameters(mapOf("key" to 1, LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10))
            )
        val tradeStrategy = mockk<TradeStrategy>()

        //when
        val tradeSession =
            TradeSession.create(
                strategyConfiguration = strategyConfiguration,
                ticker = "ABRD",
                instrumentId = "926fdfbf-4b07-47c9-8928-f49858ca33f2",
                orderLotsQuantityStrategy = orderLotsQuantityStrategy,
                tradeStrategy = tradeStrategy
            )

        //then
        tradeSession.id.shouldNotBeNull()
        tradeSession.ticker shouldBe "ABRD"
        tradeSession.instrumentId shouldBe "926fdfbf-4b07-47c9-8928-f49858ca33f2"
        tradeSession.status shouldBe TradeSessionStatus.WAITING
        tradeSession.candleInterval shouldBe CandleInterval.ONE_MIN
        tradeSession.strategy shouldBe tradeStrategy
        tradeSession.strategyType shouldBe "strategy-type"
        tradeSession.strategyParameters shouldBe StrategyParameters(
            mapOf(
                "key" to 1,
                LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10
            )
        )

        tradeSession.events.size shouldBe 1
        val domainEvent = tradeSession.events.first()
        domainEvent.shouldBeTypeOf<TradeSessionCreatedDomainEvent>()
        domainEvent.tradeSession shouldBeSameInstanceAs tradeSession
    }

    "should add candle to series" {
        //given
        val barSeries =
            BaseBarSeriesBuilder().build()
                .apply {
                    addBar(
                        barBuilder()
                            .timePeriod(Duration.ofMinutes(1))
                            .endTime(LocalDateTime.parse("2024-01-30T10:15:00").toMskInstant())
                            .openPrice(BigDecimal(100))
                            .highPrice(BigDecimal(102))
                            .lowPrice(BigDecimal(98))
                            .closePrice(BigDecimal(102))
                            .volume(10)
                            .build()
                    )
                }
        val tradeStrategy =
            spyk(TradeStrategy(barSeries, false, mockk<Strategy>())) {
                every { shouldEnter(any()) } returns false
                every { shouldExit(any(Position::class)) } returns false
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
                instrumentId = "926fdfbf-4b07-47c9-8928-f49858ca33f2"
            )
        val tradeSession =
            TradeSession(
                id = TradeSessionId(UUID.randomUUID()),
                ticker = "ABRD",
                instrumentId = "926fdfbf-4b07-47c9-8928-f49858ca33f2",
                status = TradeSessionStatus.WAITING,
                candleInterval = CandleInterval.ONE_MIN,
                orderLotsQuantityStrategy = orderLotsQuantityStrategy,
                strategy = tradeStrategy,
                strategyType = "DUMMY",
                strategyParameters = StrategyParameters(
                    mapOf(
                        "paramName" to 1,
                        LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10
                    )
                )
            )

        //when
        tradeSession.processIncomeCandle(candle)

        //then
        barSeries.barCount shouldBe 2
        val lastBar = barSeries.lastBar
        lastBar.endTime shouldBe LocalDateTime.parse("2024-01-30T07:19:00").toInstant(ZoneOffset.UTC)
    }

    "should pending enter trade session" {
        //given
        val barSeries =
            BaseBarSeriesBuilder().build()
                .apply {
                    addBar(
                        barBuilder()
                            .timePeriod(Duration.ofMinutes(1))
                            .endTime(LocalDateTime.parse("2024-01-30T10:15:00").toMskInstant())
                            .openPrice(BigDecimal(100))
                            .highPrice(BigDecimal(102))
                            .lowPrice(BigDecimal(98))
                            .closePrice(BigDecimal(102))
                            .volume(10)
                            .build()
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
                ticker = "ABRD",
                instrumentId = "926fdfbf-4b07-47c9-8928-f49858ca33f2",
                status = TradeSessionStatus.WAITING,
                candleInterval = CandleInterval.ONE_MIN,
                orderLotsQuantityStrategy = orderLotsQuantityStrategy,
                strategy = tradeStrategy,
                strategyType = "DUMMY",
                strategyParameters = StrategyParameters(
                    mapOf(
                        "paramName" to 1,
                        LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10
                    )
                )
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
                instrumentId = "926fdfbf-4b07-47c9-8928-f49858ca33f2"
            )

        //when
        tradeSession.processIncomeCandle(candle)

        //then
        tradeSession.status shouldBe TradeSessionStatus.PENDING_ENTER
        val domainEvents = tradeSession.events
        domainEvents shouldHaveSize 2
        val pendingEnterEvent = domainEvents[1]
        pendingEnterEvent.shouldBeTypeOf<TradeSessionPendedForEntryDomainEvent>()
        pendingEnterEvent.tradeSession shouldBeSameInstanceAs tradeSession
    }

    "should pending exit trade session" {
        //given
        val barSeries =
            BaseBarSeriesBuilder().build()
                .apply {
                    addBar(
                        barBuilder()
                            .timePeriod(Duration.ofMinutes(1))
                            .endTime(LocalDateTime.parse("2024-01-30T10:15:00").toMskInstant())
                            .openPrice(BigDecimal(100))
                            .highPrice(BigDecimal(102))
                            .lowPrice(BigDecimal(98))
                            .closePrice(BigDecimal(102))
                            .volume(10)
                            .build()
                    )
                }
        val tradeStrategy =
            spyk(TradeStrategy(barSeries, false, mockk<Strategy>())) {
                every { shouldEnter(any()) } returns true
                every { shouldExit(any(Position::class)) } returns true
            }
        val tradeSessionId = UUID.randomUUID()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(tradeSessionId),
                ticker = "ABRD",
                instrumentId = "926fdfbf-4b07-47c9-8928-f49858ca33f2",
                status = TradeSessionStatus.IN_POSITION,
                candleInterval = CandleInterval.ONE_MIN,
                orderLotsQuantityStrategy = orderLotsQuantityStrategy,
                currentPosition = CurrentPosition(lotsQuantity = 5, averagePrice = BigDecimal("42")),
                strategy = tradeStrategy,
                strategyType = "DUMMY",
                strategyParameters = StrategyParameters(
                    mapOf(
                        "paramName" to 1,
                        LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10
                    )
                )
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
                instrumentId = "926fdfbf-4b07-47c9-8928-f49858ca33f2"
            )

        //when
        tradeSession.processIncomeCandle(candle)

        //then
        tradeSession.status shouldBe TradeSessionStatus.PENDING_EXIT
        val domainEvents = tradeSession.events
        domainEvents shouldHaveSize 2
        val pendingExitEvent = domainEvents[1]
        pendingExitEvent.shouldBeTypeOf<TradeSessionPendedForExitDomainEvent>()
        pendingExitEvent.tradeSession shouldBeSameInstanceAs tradeSession
    }

    "should enter trade session" {
        //given
        val tradeStrategy = mockk<TradeStrategy>()
        val tradeSessionId = UUID.randomUUID()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(tradeSessionId),
                ticker = "ABRD",
                instrumentId = "926fdfbf-4b07-47c9-8928-f49858ca33f2",
                status = TradeSessionStatus.PENDING_ENTER,
                candleInterval = CandleInterval.ONE_MIN,
                orderLotsQuantityStrategy = orderLotsQuantityStrategy,
                strategy = tradeStrategy,
                strategyType = "DUMMY",
                strategyParameters = StrategyParameters(
                    mapOf(
                        "paramName" to 1,
                        LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10
                    )
                )
            )

        //when
        tradeSession.enter(10, 10, BigDecimal("42"))

        //then
        tradeSession.status shouldBe TradeSessionStatus.IN_POSITION
        tradeSession.currentPosition.lotsQuantity shouldBe 10
        tradeSession.currentPosition.averagePrice shouldBe BigDecimal("42")
        val domainEvents = tradeSession.events
        domainEvents shouldHaveSize 1
        val enteredEvent = domainEvents.first()
        enteredEvent.shouldBeTypeOf<TradeSessionEnteredDomainEvent>()
        enteredEvent.tradeSession shouldBeSameInstanceAs tradeSession
        enteredEvent.lotsRequested shouldBe 10
    }

    "should exit trade session" {
        //given
        val tradeStrategy = mockk<TradeStrategy>()
        val tradeSessionId = UUID.randomUUID()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(tradeSessionId),
                ticker = "ABRD",
                instrumentId = "926fdfbf-4b07-47c9-8928-f49858ca33f2",
                status = TradeSessionStatus.PENDING_EXIT,
                candleInterval = CandleInterval.ONE_MIN,
                orderLotsQuantityStrategy = orderLotsQuantityStrategy,
                currentPosition = CurrentPosition(5, BigDecimal(100)),
                strategy = tradeStrategy,
                strategyType = "DUMMY",
                strategyParameters = StrategyParameters(
                    mapOf(
                        "paramName" to 1,
                        LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10
                    )
                )
            )

        //when
        tradeSession.exit(5)

        //then
        tradeSession.status shouldBe TradeSessionStatus.WAITING
        tradeSession.currentPosition.lotsQuantity shouldBe 0
        tradeSession.currentPosition.averagePrice shouldBe BigDecimal.ZERO
        val domainEvents = tradeSession.events
        domainEvents shouldHaveSize 1
        val exitedEvent = domainEvents.first()
        exitedEvent.shouldBeTypeOf<TradeSessionExitedDomainEvent>()
        exitedEvent.tradeSession shouldBeSameInstanceAs tradeSession
        exitedEvent.lotsRequested shouldBe 5
        exitedEvent.lotsExecuted shouldBe 5
    }

    "should resume trade session" - {
        data class TestParameters(
            val currentStatus: TradeSessionStatus,
            val targetStatus: TradeSessionStatus,
            val currentPositionLotsQuantity: Int,
            val currentPositionAveragePrice: BigDecimal
        )
        withData(
            nameFn = { "current = ${it.currentStatus}, target = ${it.targetStatus}, is in position = ${it.currentPositionLotsQuantity > 0}" },
            TestParameters(TradeSessionStatus.PENDING_ENTER, TradeSessionStatus.WAITING, 0, BigDecimal.ZERO),
            TestParameters(TradeSessionStatus.PENDING_EXIT, TradeSessionStatus.IN_POSITION, 10, BigDecimal("42")),
            TestParameters(TradeSessionStatus.STOPPED, TradeSessionStatus.IN_POSITION, 10, BigDecimal("42")),
            TestParameters(TradeSessionStatus.STOPPED, TradeSessionStatus.WAITING, 0, BigDecimal.ZERO)
        ) { (currentStatus, targetStatus, currentPositionLotsQuantity, currentPositionAveragePrice) ->
            //given
            val tradeStrategy = mockk<TradeStrategy>()
            val tradeSessionId = UUID.randomUUID()
            val tradeSession =
                TradeSession(
                    id = TradeSessionId(tradeSessionId),
                    ticker = "ABRD",
                    instrumentId = "926fdfbf-4b07-47c9-8928-f49858ca33f2",
                    status = currentStatus,
                    candleInterval = CandleInterval.ONE_MIN,
                    orderLotsQuantityStrategy = orderLotsQuantityStrategy,
                    currentPosition = CurrentPosition(
                        lotsQuantity = currentPositionLotsQuantity,
                        averagePrice = currentPositionAveragePrice
                    ),
                    strategy = tradeStrategy,
                    strategyType = "DUMMY",
                    strategyParameters = StrategyParameters(
                        mapOf(
                            "paramName" to 1,
                            LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10
                        )
                    )
                )

            //when
            tradeSession.resume()

            //then
            tradeSession.status shouldBe targetStatus
            val domainEvents = tradeSession.events
            domainEvents shouldHaveSize 1
            val stoppedEvent = domainEvents.first()
            stoppedEvent.shouldBeTypeOf<TradeSessionResumedDomainEvent>()
            stoppedEvent.tradeSession shouldBeSameInstanceAs tradeSession
        }
    }

    "should stop trade session" {
        //given
        val tradeStrategy = mockk<TradeStrategy>()
        val tradeSessionId = UUID.randomUUID()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(tradeSessionId),
                ticker = "ABRD",
                instrumentId = "926fdfbf-4b07-47c9-8928-f49858ca33f2",
                status = TradeSessionStatus.WAITING,
                candleInterval = CandleInterval.ONE_MIN,
                orderLotsQuantityStrategy = orderLotsQuantityStrategy,
                strategy = tradeStrategy,
                strategyType = "DUMMY",
                strategyParameters = StrategyParameters(
                    mapOf(
                        "paramName" to 1,
                        LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10
                    )
                )
            )

        //when
        tradeSession.stop()

        //then
        tradeSession.status shouldBe TradeSessionStatus.STOPPED
        val domainEvents = tradeSession.events
        domainEvents shouldHaveSize 1
        val stoppedEvent = domainEvents.first()
        stoppedEvent.shouldBeTypeOf<TradeSessionStoppedDomainEvent>()
        stoppedEvent.tradeSession shouldBeSameInstanceAs tradeSession
    }

    "should throw TradeSessionDomainException while adding new candle when new candle date intersects series dates" {
        //given
        val barSeries =
            BaseBarSeriesBuilder().build()
                .apply {
                    addBar(
                        barBuilder()
                            .timePeriod(Duration.ofMinutes(1))
                            .endTime(LocalDateTime.parse("2024-01-30T10:15:00").toMskInstant())
                            .openPrice(BigDecimal(100))
                            .highPrice(BigDecimal(102))
                            .lowPrice(BigDecimal(98))
                            .closePrice(BigDecimal(102))
                            .volume(10)
                            .build()
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
                instrumentId = "926fdfbf-4b07-47c9-8928-f49858ca33f2"
            )
        val tradeSessionId = UUID.randomUUID()
        val tradeSession =
            TradeSession(
                id = TradeSessionId(tradeSessionId),
                ticker = "ABRD",
                instrumentId = "926fdfbf-4b07-47c9-8928-f49858ca33f2",
                status = TradeSessionStatus.WAITING,
                candleInterval = CandleInterval.ONE_MIN,
                orderLotsQuantityStrategy = orderLotsQuantityStrategy,
                strategy = tradeStrategy,
                strategyType = "DUMMY",
                strategyParameters = StrategyParameters(
                    mapOf(
                        "paramName" to 1,
                        LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10
                    )
                )
            )

        //when
        val ex = shouldThrow<TradeSessionDomainException> { tradeSession.processIncomeCandle(candle) }

        //then
        ex shouldHaveMessage "Unable to process income candle 2024-01-30T10:14: new candle date intersects trade session " +
                "[id=$tradeSessionId, ticker=ABRD, strategyType=DUMMY, candleInterval=ONE_MIN, status=WAITING] candle series with last date 2024-01-30T10:15"
    }

})