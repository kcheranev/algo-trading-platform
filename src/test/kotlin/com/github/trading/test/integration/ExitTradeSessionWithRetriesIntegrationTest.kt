package com.github.trading.test.integration

import com.github.trading.common.date.toMskZonedDateTime
import com.github.trading.core.port.income.marketdata.ProcessIncomeCandleCommand
import com.github.trading.core.service.MarketDataProcessingService
import com.github.trading.core.strategy.lotsquantity.LOTS_QUANTITY_STRATEGY_PARAMETER_NAME
import com.github.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyType
import com.github.trading.domain.entity.TradeSessionStatus
import com.github.trading.domain.model.Candle
import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.Instrument
import com.github.trading.domain.model.Position
import com.github.trading.domain.model.TradeDirection
import com.github.trading.domain.model.TradeStrategy
import com.github.trading.infra.adapter.outcome.persistence.entity.TradeOrderEntity
import com.github.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import com.github.trading.infra.adapter.outcome.persistence.impl.TradeStrategyCache
import com.github.trading.infra.adapter.outcome.persistence.model.MapWrapper
import com.github.trading.test.IntegrationTest
import com.github.trading.test.stub.grpc.OrdersBrokerGrpcStub
import com.github.trading.test.stub.grpc.UsersBrokerGrpcStub
import com.github.trading.test.stub.http.TelegramNotificationHttpStub
import com.github.trading.test.util.MarketDataSubscriptionInitializer
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.ta4j.core.BaseBar
import org.ta4j.core.BaseBarSeriesBuilder
import org.ta4j.core.Strategy
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@IntegrationTest
class ExitTradeSessionWithRetriesIntegrationTest(
    private val marketDataProcessingService: MarketDataProcessingService,
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val tradeStrategyCache: TradeStrategyCache,
    private val marketDataSubscriptionInitializer: MarketDataSubscriptionInitializer,
    private val telegramNotificationHttpStub: TelegramNotificationHttpStub,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    val testName = "exit-trade-session-with-retries"

    val usersBrokerGrpcStub = UsersBrokerGrpcStub(testName)

    val ordersBrokerGrpcStub = OrdersBrokerGrpcStub(testName)

    "should exit trade session with retries" {
        //given
        val tradeSessionId = UUID.randomUUID()
        jdbcTemplate.insert(
            TradeSessionEntity(
                id = tradeSessionId,
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.IN_POSITION,
                candleInterval = CandleInterval.ONE_MIN,
                orderLotsQuantityStrategyType = OrderLotsQuantityStrategyType.HARDCODED,
                positionLotsQuantity = 10,
                positionAveragePrice = BigDecimal("100"),
                strategyType = "DUMMY_LONG",
                strategyParameters = MapWrapper(mapOf("paramName" to 1, LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10))
            )
        )
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
                every { shouldExit(any(Position::class)) } returns true
            }
        tradeStrategyCache.put(tradeSessionId, tradeStrategy)
        marketDataSubscriptionInitializer.addSubscription(
            Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
            CandleInterval.ONE_MIN
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
        usersBrokerGrpcStub.stubForGetAccounts("get-accounts.json")
        ordersBrokerGrpcStub.stubForPostSellOrder(
            "post-sell-order-requested-10-executed-4.json",
            mapOf("quantity" to "10")
        )
        ordersBrokerGrpcStub.stubForPostSellOrder(
            "post-sell-order-requested-6-executed-4.json",
            mapOf("quantity" to "6")
        )
        ordersBrokerGrpcStub.stubForPostSellOrder(
            "post-sell-order-requested-2-executed-2.json",
            mapOf("quantity" to "2")
        )
        telegramNotificationHttpStub.stubForSendNotification()

        //when
        marketDataProcessingService.processIncomeCandle(ProcessIncomeCandleCommand(candle))

        //then
        ordersBrokerGrpcStub.verifyForPostSellOrder("post-sell-order-quantity-10.json", mapOf("quantity" to "10"))
        ordersBrokerGrpcStub.verifyForPostSellOrder("post-sell-order-quantity-6.json", mapOf("quantity" to "6"))
        ordersBrokerGrpcStub.verifyForPostSellOrder("post-sell-order-quantity-2.json", mapOf("quantity" to "2"))

        val tradeSession = jdbcTemplate.findById(tradeSessionId, TradeSessionEntity::class.java)
        tradeSession.status shouldBe TradeSessionStatus.WAITING
        tradeSession.positionLotsQuantity shouldBe 0
        tradeSession.positionAveragePrice shouldBe BigDecimal.ZERO

        val tradeOrders = jdbcTemplate.findAll(TradeOrderEntity::class.java)
        tradeOrders shouldHaveSize 3
        val sortedTradeOrders = tradeOrders.sortedBy { it.date }
        sortedTradeOrders[0].lotsQuantity shouldBe 4
        sortedTradeOrders[0].direction shouldBe TradeDirection.SELL
        sortedTradeOrders[1].lotsQuantity shouldBe 4
        sortedTradeOrders[1].direction shouldBe TradeDirection.SELL
        sortedTradeOrders[2].lotsQuantity shouldBe 2
        sortedTradeOrders[2].direction shouldBe TradeDirection.SELL
    }

    "should exit trade session with retries when executed lots quantity not equals to requested lots quantity" {
        //given
        val tradeSessionId = UUID.randomUUID()
        jdbcTemplate.insert(
            TradeSessionEntity(
                id = tradeSessionId,
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.IN_POSITION,
                candleInterval = CandleInterval.ONE_MIN,
                orderLotsQuantityStrategyType = OrderLotsQuantityStrategyType.HARDCODED,
                positionLotsQuantity = 10,
                positionAveragePrice = BigDecimal("100"),
                strategyType = "DUMMY_LONG",
                strategyParameters = MapWrapper(mapOf("paramName" to 1, LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10))
            )
        )
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
                every { shouldExit(any(Position::class)) } returns true
            }
        tradeStrategyCache.put(tradeSessionId, tradeStrategy)
        marketDataSubscriptionInitializer.addSubscription(
            Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
            CandleInterval.ONE_MIN
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
        usersBrokerGrpcStub.stubForGetAccounts("get-accounts.json")
        ordersBrokerGrpcStub.stubForPostSellOrder(
            "post-sell-order-requested-10-executed-4.json",
            mapOf("quantity" to "10")
        )
        ordersBrokerGrpcStub.stubForPostSellOrder(
            "post-sell-order-requested-6-executed-4.json",
            mapOf("quantity" to "6")
        )
        ordersBrokerGrpcStub.stubForPostSellOrder(
            "post-sell-order-requested-2-executed-1.json",
            mapOf("quantity" to "2")
        )
        telegramNotificationHttpStub.stubForSendNotification()

        //when
        marketDataProcessingService.processIncomeCandle(ProcessIncomeCandleCommand(candle))

        //then
        ordersBrokerGrpcStub.verifyForPostSellOrder("post-sell-order-quantity-10.json", mapOf("quantity" to "10"))
        ordersBrokerGrpcStub.verifyForPostSellOrder("post-sell-order-quantity-6.json", mapOf("quantity" to "6"))
        ordersBrokerGrpcStub.verifyForPostSellOrder("post-sell-order-quantity-2.json", mapOf("quantity" to "2"))

        val tradeSession = jdbcTemplate.findById(tradeSessionId, TradeSessionEntity::class.java)
        tradeSession.status shouldBe TradeSessionStatus.WAITING
        tradeSession.positionLotsQuantity shouldBe 0
        tradeSession.positionAveragePrice shouldBe BigDecimal.ZERO

        val tradeOrders = jdbcTemplate.findAll(TradeOrderEntity::class.java)
        tradeOrders shouldHaveSize 3
        val sortedTradeOrders = tradeOrders.sortedBy { it.date }
        sortedTradeOrders[0].lotsQuantity shouldBe 4
        sortedTradeOrders[0].direction shouldBe TradeDirection.SELL
        sortedTradeOrders[1].lotsQuantity shouldBe 4
        sortedTradeOrders[1].direction shouldBe TradeDirection.SELL
        sortedTradeOrders[2].lotsQuantity shouldBe 1
        sortedTradeOrders[2].direction shouldBe TradeDirection.SELL
    }

    "should trade session still waiting for entry when there are no executed orders" {
        //given
        val tradeSessionId = UUID.randomUUID()
        jdbcTemplate.insert(
            TradeSessionEntity(
                id = tradeSessionId,
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.IN_POSITION,
                candleInterval = CandleInterval.ONE_MIN,
                orderLotsQuantityStrategyType = OrderLotsQuantityStrategyType.HARDCODED,
                positionLotsQuantity = 10,
                positionAveragePrice = BigDecimal("42"),
                strategyType = "DUMMY_LONG",
                strategyParameters = MapWrapper(mapOf("paramName" to 1, LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10))
            )
        )
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
                every { shouldExit(any(Position::class)) } returns true
            }
        tradeStrategyCache.put(tradeSessionId, tradeStrategy)
        marketDataSubscriptionInitializer.addSubscription(
            Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
            CandleInterval.ONE_MIN
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
        usersBrokerGrpcStub.stubForGetAccounts("get-accounts.json")
        ordersBrokerGrpcStub.stubForPostSellOrder(
            "post-sell-order-requested-10-executed-0.json",
            mapOf("quantity" to "10")
        )
        telegramNotificationHttpStub.stubForSendNotification()

        //when
        marketDataProcessingService.processIncomeCandle(ProcessIncomeCandleCommand(candle))

        //then
        ordersBrokerGrpcStub.verifyForPostSellOrder("post-sell-order-quantity-10.json", mapOf("quantity" to "10"), 3)

        val tradeSession = jdbcTemplate.findById(tradeSessionId, TradeSessionEntity::class.java)
        tradeSession.status shouldBe TradeSessionStatus.IN_POSITION
        tradeSession.positionLotsQuantity shouldBe 10
        tradeSession.positionAveragePrice shouldBe BigDecimal("42")

        val tradeOrders = jdbcTemplate.findAll(TradeOrderEntity::class.java)
        tradeOrders.shouldBeEmpty()
    }

    "should trade session still waiting for entry when when post order is failed" {
        //given
        val tradeSessionId = UUID.randomUUID()
        jdbcTemplate.insert(
            TradeSessionEntity(
                id = tradeSessionId,
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.IN_POSITION,
                candleInterval = CandleInterval.ONE_MIN,
                orderLotsQuantityStrategyType = OrderLotsQuantityStrategyType.HARDCODED,
                positionLotsQuantity = 10,
                positionAveragePrice = BigDecimal("42"),
                strategyType = "DUMMY_LONG",
                strategyParameters = MapWrapper(mapOf("paramName" to 1, LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10))
            )
        )
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
                every { shouldExit(any(Position::class)) } returns true
            }
        tradeStrategyCache.put(tradeSessionId, tradeStrategy)
        marketDataSubscriptionInitializer.addSubscription(
            Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
            CandleInterval.ONE_MIN
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
        usersBrokerGrpcStub.stubForGetAccounts("get-accounts.json")
        ordersBrokerGrpcStub.stubForPostOrderFailed()
        telegramNotificationHttpStub.stubForSendNotification()

        //when
        marketDataProcessingService.processIncomeCandle(ProcessIncomeCandleCommand(candle))

        //then
        ordersBrokerGrpcStub.verifyForPostSellOrder("post-sell-order-quantity-10.json", mapOf("quantity" to "10"), 3)

        val tradeSession = jdbcTemplate.findById(tradeSessionId, TradeSessionEntity::class.java)
        tradeSession.status shouldBe TradeSessionStatus.IN_POSITION
        tradeSession.positionLotsQuantity shouldBe 10
        tradeSession.positionAveragePrice shouldBe BigDecimal("42")

        val tradeOrders = jdbcTemplate.findAll(TradeOrderEntity::class.java)
        tradeOrders.shouldBeEmpty()
    }

})