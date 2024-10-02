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
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.request.StartTradeSessionRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.response.StartTradeSessionResponseDto
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
class TradeProcessShortE2eTest(
    private val marketDataProcessingService: MarketDataProcessingService,
    private val testRestTemplate: TestRestTemplate,
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val telegramNotificationHttpStub: TelegramNotificationHttpStub,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    val testName = "trade-process-short-e2e"

    val marketDataBrokerGrpcStub = MarketDataBrokerGrpcStub(testName)

    val usersBrokerGrpcStub = UsersBrokerGrpcStub(testName)

    val ordersBrokerGrpcStub = OrdersBrokerGrpcStub(testName)

    "should execute short trade process" {
        //create strategy configuration
        val strategyConfiguration =
            jdbcTemplate.insert(
                StrategyConfigurationEntity(
                    id = UUID.randomUUID(),
                    name = "dummy",
                    type = "DUMMY_SHORT",
                    candleInterval = CandleInterval.ONE_MIN,
                    parameters = MapWrapper(emptyMap())
                )
            )

        //start trade session
        marketDataBrokerGrpcStub.stubForGetCandles("get-candles.json")
        val startTradeSessionResponse = testRestTemplate.postForEntity(
            "/trade-sessions",
            StartTradeSessionRequestDto(
                strategyConfiguration.id,
                4,
                InstrumentDto("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
            ),
            StartTradeSessionResponseDto::class.java
        )
        startTradeSessionResponse.statusCode shouldBe HttpStatus.OK

        //income candle event
        usersBrokerGrpcStub.stubForGetAccounts("get-accounts.json")
        ordersBrokerGrpcStub.stubForPostBuyOrder("post-buy-order.json")
        ordersBrokerGrpcStub.stubForPostSellOrder("post-sell-order.json")
        telegramNotificationHttpStub.stubForSendNotification()

        marketDataProcessingService.processIncomeCandle(
            ProcessIncomeCandleCommand(
                Candle(
                    interval = CandleInterval.ONE_MIN,
                    openPrice = BigDecimal(106),
                    closePrice = BigDecimal(105),
                    highestPrice = BigDecimal(107),
                    lowestPrice = BigDecimal(105),
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
                    openPrice = BigDecimal(105),
                    closePrice = BigDecimal(104),
                    highestPrice = BigDecimal(106),
                    lowestPrice = BigDecimal(104),
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
                    openPrice = BigDecimal(104),
                    closePrice = BigDecimal(102),
                    highestPrice = BigDecimal(105),
                    lowestPrice = BigDecimal(102),
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
                    closePrice = BigDecimal(101),
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
                    openPrice = BigDecimal(101),
                    closePrice = BigDecimal(99),
                    highestPrice = BigDecimal(106),
                    lowestPrice = BigDecimal(99),
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
        val sellOrder = orders[0]
        with(sellOrder) {
            ticker shouldBe "SBER"
            instrumentId shouldBe "e6123145-9665-43e0-8413-cd61b8aa9b1"
            date shouldBe LocalDateTime.parse("2024-01-30T10:15:30")
            lotsQuantity shouldBe 4
            totalPrice shouldBe BigDecimal("423.000000000")
            executedCommission shouldBe BigDecimal("5.000000000")
            direction shouldBe TradeDirection.SELL
            tradeSessionId.shouldNotBeNull()
        }
        val buyOrder = orders[1]
        with(buyOrder) {
            ticker shouldBe "SBER"
            instrumentId shouldBe "e6123145-9665-43e0-8413-cd61b8aa9b1"
            date shouldBe LocalDateTime.parse("2024-01-30T10:15:30")
            lotsQuantity shouldBe 4
            totalPrice shouldBe BigDecimal("404.000000000")
            executedCommission shouldBe BigDecimal("7.000000000")
            direction shouldBe TradeDirection.BUY
            tradeSessionId.shouldNotBeNull()
        }

        //check trade session
        val tradeSessions = jdbcTemplate.findAll(TradeSessionEntity::class.java)
        tradeSessions shouldHaveSize 1
        tradeSessions.first().status shouldBe TradeSessionStatus.WAITING
    }

})