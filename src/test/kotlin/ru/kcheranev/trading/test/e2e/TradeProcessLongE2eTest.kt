package ru.kcheranev.trading.test.e2e

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.http.HttpStatus
import ru.kcheranev.trading.core.port.income.marketdata.ProcessIncomeCandleCommand
import ru.kcheranev.trading.core.service.MarketDataProcessingService
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.TradeDirection
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.common.InstrumentDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.request.CreateTradeSessionRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.response.CreateTradeSessionResponseDto
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeOrderEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import ru.kcheranev.trading.test.IntegrationTest
import ru.kcheranev.trading.test.stub.grpc.MarketDataBrokerGrpcStub
import ru.kcheranev.trading.test.stub.grpc.OrdersBrokerGrpcStub
import ru.kcheranev.trading.test.stub.grpc.UsersBrokerGrpcStub
import ru.kcheranev.trading.test.stub.http.TelegramNotificationHttpStub
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@IntegrationTest
class TradeProcessLongE2eTest(
    private val marketDataProcessingService: MarketDataProcessingService,
    private val testRestTemplate: TestRestTemplate,
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val telegramNotificationHttpStub: TelegramNotificationHttpStub,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    val testName = "trade-process-long-e2e"

    val marketDataBrokerGrpcStub = MarketDataBrokerGrpcStub(testName)

    val usersBrokerGrpcStub = UsersBrokerGrpcStub(testName)

    val ordersBrokerGrpcStub = OrdersBrokerGrpcStub(testName)

    "should execute long trade process" {
        //create strategy configuration
        val strategyConfiguration =
            jdbcTemplate.insert(
                StrategyConfigurationEntity(
                    id = UUID.randomUUID(),
                    name = "dummy",
                    type = "DUMMY_LONG",
                    candleInterval = CandleInterval.ONE_MIN,
                    parameters = MapWrapper(emptyMap())
                )
            )

        //start trade session
        marketDataBrokerGrpcStub.stubForGetCandles("get-candles.json")
        val createTradeSessionResponse = testRestTemplate.postForEntity(
            "/trade-sessions",
            CreateTradeSessionRequestDto(
                strategyConfiguration.id,
                4,
                InstrumentDto("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
            ),
            CreateTradeSessionResponseDto::class.java
        )
        createTradeSessionResponse.statusCode shouldBe HttpStatus.OK

        //income candle event
        usersBrokerGrpcStub.stubForGetAccounts("get-accounts.json")
        ordersBrokerGrpcStub.stubForPostBuyOrder("post-buy-order.json")
        ordersBrokerGrpcStub.stubForPostSellOrder("post-sell-order.json")
        telegramNotificationHttpStub.stubForSendNotification()

        marketDataProcessingService.processIncomeCandle(
            ProcessIncomeCandleCommand(
                Candle(
                    interval = CandleInterval.ONE_MIN,
                    openPrice = BigDecimal(99),
                    closePrice = BigDecimal(100),
                    highestPrice = BigDecimal(100),
                    lowestPrice = BigDecimal(98),
                    volume = 10,
                    endDateTime = LocalDateTime.parse("2024-01-30T10:17:00"),
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
                )
            )
        )

        marketDataProcessingService.processIncomeCandle(
            ProcessIncomeCandleCommand(
                Candle(
                    interval = CandleInterval.ONE_MIN,
                    openPrice = BigDecimal(100),
                    closePrice = BigDecimal(101),
                    highestPrice = BigDecimal(101),
                    lowestPrice = BigDecimal(99),
                    volume = 10,
                    endDateTime = LocalDateTime.parse("2024-01-30T10:18:00"),
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
                )
            )
        )

        marketDataProcessingService.processIncomeCandle(
            ProcessIncomeCandleCommand(
                Candle(
                    interval = CandleInterval.ONE_MIN,
                    openPrice = BigDecimal(101),
                    closePrice = BigDecimal(102),
                    highestPrice = BigDecimal(102),
                    lowestPrice = BigDecimal(100),
                    volume = 10,
                    endDateTime = LocalDateTime.parse("2024-01-30T10:19:00"),
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
                )
            )
        )

        marketDataProcessingService.processIncomeCandle(
            ProcessIncomeCandleCommand(
                Candle(
                    interval = CandleInterval.ONE_MIN,
                    openPrice = BigDecimal(102),
                    closePrice = BigDecimal(103),
                    highestPrice = BigDecimal(103),
                    lowestPrice = BigDecimal(101),
                    volume = 10,
                    endDateTime = LocalDateTime.parse("2024-01-30T10:20:00"),
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
                )
            )
        )

        marketDataProcessingService.processIncomeCandle(
            ProcessIncomeCandleCommand(
                Candle(
                    interval = CandleInterval.ONE_MIN,
                    openPrice = BigDecimal(103),
                    closePrice = BigDecimal(106),
                    highestPrice = BigDecimal(106),
                    lowestPrice = BigDecimal(102),
                    volume = 10,
                    endDateTime = LocalDateTime.parse("2024-01-30T10:21:00"),
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
                )
            )
        )

        ordersBrokerGrpcStub.verifyForPostBuyOrder("post-buy-order.json")
        ordersBrokerGrpcStub.verifyForPostSellOrder("post-sell-order.json")

        //check orders
        val orders = jdbcTemplate.findAll(TradeOrderEntity::class.java).toList()
        orders shouldHaveSize 2
        val buyOrder = orders[0]
        with(buyOrder) {
            ticker shouldBe "SBER"
            instrumentId shouldBe "e6123145-9665-43e0-8413-cd61b8aa9b1"
            date shouldBe LocalDateTime.parse("2024-01-30T10:15:30")
            lotsQuantity shouldBe 4
            totalPrice shouldBe BigDecimal("413.000000000")
            executedCommission shouldBe BigDecimal("7.000000000")
            direction shouldBe TradeDirection.BUY
            tradeSessionId.shouldNotBeNull()
        }
        val sellOrder = orders[1]
        with(sellOrder) {
            ticker shouldBe "SBER"
            instrumentId shouldBe "e6123145-9665-43e0-8413-cd61b8aa9b1"
            date shouldBe LocalDateTime.parse("2024-01-30T10:15:30")
            lotsQuantity shouldBe 4
            totalPrice shouldBe BigDecimal("434.000000000")
            executedCommission shouldBe BigDecimal("5.000000000")
            direction shouldBe TradeDirection.SELL
            tradeSessionId.shouldNotBeNull()
        }

        //check trade session
        val tradeSessions = jdbcTemplate.findAll(TradeSessionEntity::class.java)
        tradeSessions shouldHaveSize 1
        tradeSessions.first().status shouldBe TradeSessionStatus.WAITING
    }

})