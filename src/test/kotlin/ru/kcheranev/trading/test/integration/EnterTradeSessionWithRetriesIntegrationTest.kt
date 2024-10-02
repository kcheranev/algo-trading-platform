package ru.kcheranev.trading.test.integration

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
import ru.kcheranev.trading.common.date.toMskZonedDateTime
import ru.kcheranev.trading.core.port.income.marketdata.ProcessIncomeCandleCommand
import ru.kcheranev.trading.core.service.MarketDataProcessingService
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.TradeStrategy
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeOrderEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.impl.TradeStrategyCache
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import ru.kcheranev.trading.test.IntegrationTest
import ru.kcheranev.trading.test.stub.grpc.OrdersBrokerGrpcStub
import ru.kcheranev.trading.test.stub.grpc.UsersBrokerGrpcStub
import ru.kcheranev.trading.test.stub.http.TelegramNotificationHttpStub
import ru.kcheranev.trading.test.util.MarketDataSubscriptionInitializer
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@IntegrationTest
class EnterTradeSessionWithRetriesIntegrationTest(
    private val marketDataProcessingService: MarketDataProcessingService,
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val tradeStrategyCache: TradeStrategyCache,
    private val marketDataSubscriptionInitializer: MarketDataSubscriptionInitializer,
    private val telegramNotificationHttpStub: TelegramNotificationHttpStub,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    val testName = "enter-trade-session-with-retries"

    val usersBrokerGrpcStub = UsersBrokerGrpcStub(testName)

    val ordersBrokerGrpcStub = OrdersBrokerGrpcStub(testName)

    "should enter trade session with retries" {
        //given
        val tradeSessionId = UUID.randomUUID()
        jdbcTemplate.insert(
            TradeSessionEntity(
                id = tradeSessionId,
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.parse("2024-01-01T10:15:00"),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                lotsQuantityInPosition = 0,
                strategyType = "DUMMY",
                strategyParameters = MapWrapper(mapOf("paramName" to 1))
            )
        )
        val barSeries = BaseBarSeriesBuilder().build()
        barSeries.addBar(
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
        val tradeStrategy =
            spyk(TradeStrategy(barSeries, false, mockk<Strategy>())) {
                every { shouldEnter(any()) } returns true
                every { shouldExit(any()) } returns false
            }
        tradeStrategyCache.put(tradeSessionId, tradeStrategy)
        marketDataSubscriptionInitializer.init(
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
        ordersBrokerGrpcStub.stubForPostBuyOrder(
            "post-buy-order-requested-10-executed-4.json",
            mapOf("quantity" to "10")
        )
        ordersBrokerGrpcStub.stubForPostBuyOrder(
            "post-buy-order-requested-6-executed-4.json",
            mapOf("quantity" to "6")
        )
        ordersBrokerGrpcStub.stubForPostBuyOrder(
            "post-buy-order-requested-2-executed-2.json",
            mapOf("quantity" to "2")
        )
        telegramNotificationHttpStub.stubForSendNotification()

        //when
        marketDataProcessingService.processIncomeCandle(ProcessIncomeCandleCommand(candle))

        //then
        ordersBrokerGrpcStub.verifyForPostBuyOrder("post-buy-order-quantity-10.json", mapOf("quantity" to "10"))
        ordersBrokerGrpcStub.verifyForPostBuyOrder("post-buy-order-quantity-6.json", mapOf("quantity" to "6"))
        ordersBrokerGrpcStub.verifyForPostBuyOrder("post-buy-order-quantity-2.json", mapOf("quantity" to "2"))

        val tradeSession = jdbcTemplate.findById(tradeSessionId, TradeSessionEntity::class.java)
        tradeSession.status shouldBe TradeSessionStatus.IN_POSITION
        tradeSession.lotsQuantityInPosition shouldBe 10

        val tradeOrders = jdbcTemplate.findAll(TradeOrderEntity::class.java)
        tradeOrders shouldHaveSize 3
        val sortedTradeOrders = tradeOrders.sortedBy { it.date }
        sortedTradeOrders[0].lotsQuantity shouldBe 4
        sortedTradeOrders[1].lotsQuantity shouldBe 4
        sortedTradeOrders[2].lotsQuantity shouldBe 2
    }

    "should enter trade session with retries when executed lots quantity not equals to requested lots quantity" {
        //given
        val tradeSessionId = UUID.randomUUID()
        jdbcTemplate.insert(
            TradeSessionEntity(
                id = tradeSessionId,
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.parse("2024-01-01T10:15:00"),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                lotsQuantityInPosition = 0,
                strategyType = "DUMMY",
                strategyParameters = MapWrapper(mapOf("paramName" to 1))
            )
        )
        val barSeries = BaseBarSeriesBuilder().build()
        barSeries.addBar(
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
        val tradeStrategy =
            spyk(TradeStrategy(barSeries, false, mockk<Strategy>())) {
                every { shouldEnter(any()) } returns true
                every { shouldExit(any()) } returns false
            }
        tradeStrategyCache.put(tradeSessionId, tradeStrategy)
        marketDataSubscriptionInitializer.init(
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
        ordersBrokerGrpcStub.stubForPostBuyOrder(
            "post-buy-order-requested-10-executed-4.json",
            mapOf("quantity" to "10")
        )
        ordersBrokerGrpcStub.stubForPostBuyOrder(
            "post-buy-order-requested-6-executed-4.json",
            mapOf("quantity" to "6")
        )
        ordersBrokerGrpcStub.stubForPostBuyOrder(
            "post-buy-order-requested-2-executed-1.json",
            mapOf("quantity" to "2")
        )
        telegramNotificationHttpStub.stubForSendNotification()

        //when
        marketDataProcessingService.processIncomeCandle(ProcessIncomeCandleCommand(candle))

        //then
        ordersBrokerGrpcStub.verifyForPostBuyOrder("post-buy-order-quantity-10.json", mapOf("quantity" to "10"))
        ordersBrokerGrpcStub.verifyForPostBuyOrder("post-buy-order-quantity-6.json", mapOf("quantity" to "6"))
        ordersBrokerGrpcStub.verifyForPostBuyOrder("post-buy-order-quantity-2.json", mapOf("quantity" to "2"))

        val tradeSession = jdbcTemplate.findById(tradeSessionId, TradeSessionEntity::class.java)
        tradeSession.status shouldBe TradeSessionStatus.IN_POSITION
        tradeSession.lotsQuantityInPosition shouldBe 9

        val tradeOrders = jdbcTemplate.findAll(TradeOrderEntity::class.java)
        tradeOrders shouldHaveSize 3
        val sortedTradeOrders = tradeOrders.sortedBy { it.date }
        sortedTradeOrders[0].lotsQuantity shouldBe 4
        sortedTradeOrders[1].lotsQuantity shouldBe 4
        sortedTradeOrders[2].lotsQuantity shouldBe 1
    }

    "should trade session waiting for entry when there are no executed orders" {
        //given
        val tradeSessionId = UUID.randomUUID()
        jdbcTemplate.insert(
            TradeSessionEntity(
                id = tradeSessionId,
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.parse("2024-01-01T10:15:00"),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                lotsQuantityInPosition = 0,
                strategyType = "DUMMY",
                strategyParameters = MapWrapper(mapOf("paramName" to 1))
            )
        )
        val barSeries = BaseBarSeriesBuilder().build()
        barSeries.addBar(
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
        val tradeStrategy =
            spyk(TradeStrategy(barSeries, false, mockk<Strategy>())) {
                every { shouldEnter(any()) } returns true
                every { shouldExit(any()) } returns false
            }
        tradeStrategyCache.put(tradeSessionId, tradeStrategy)
        marketDataSubscriptionInitializer.init(
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
        ordersBrokerGrpcStub.stubForPostBuyOrder(
            "post-buy-order-requested-10-executed-0.json",
            mapOf("quantity" to "10")
        )
        telegramNotificationHttpStub.stubForSendNotification()

        //when
        marketDataProcessingService.processIncomeCandle(ProcessIncomeCandleCommand(candle))

        //then
        ordersBrokerGrpcStub.verifyForPostBuyOrder("post-buy-order-quantity-10.json", mapOf("quantity" to "10"), 3)

        val tradeSession = jdbcTemplate.findById(tradeSessionId, TradeSessionEntity::class.java)
        tradeSession.status shouldBe TradeSessionStatus.WAITING
        tradeSession.lotsQuantityInPosition shouldBe 0

        val tradeOrders = jdbcTemplate.findAll(TradeOrderEntity::class.java)
        tradeOrders.shouldBeEmpty()
    }

})