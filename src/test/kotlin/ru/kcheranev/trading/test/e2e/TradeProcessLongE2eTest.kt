package ru.kcheranev.trading.test.e2e

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import ru.kcheranev.trading.core.port.income.trading.ProcessIncomeCandleCommand
import ru.kcheranev.trading.core.service.TradeService
import ru.kcheranev.trading.domain.entity.TradeDirection
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.income.web.model.request.InstrumentDto
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StartTradeSessionRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.response.StartTradeSessionResponse
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.impl.TradeSessionCache
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.TradeOrderRepository
import ru.kcheranev.trading.test.IntegrationTest
import ru.kcheranev.trading.test.stub.grpc.MarketDataBrokerGrpcStub
import ru.kcheranev.trading.test.stub.grpc.OrdersBrokerGrpcStub
import ru.kcheranev.trading.test.stub.grpc.UsersBrokerGrpcStub
import ru.kcheranev.trading.test.stub.http.TelegramNotificationHttpStub
import java.math.BigDecimal
import java.time.LocalDateTime

@IntegrationTest
class TradeProcessLongE2eTest(
    private val tradeService: TradeService,
    private val testRestTemplate: TestRestTemplate,
    private val tradeSessionCache: TradeSessionCache,
    private val tradeOrderRepository: TradeOrderRepository,
    private val strategyConfigurationRepository: StrategyConfigurationRepository,
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
            strategyConfigurationRepository.save(
                StrategyConfigurationEntity(
                    null,
                    "DUMMY",
                    4,
                    CandleInterval.ONE_MIN,
                    MapWrapper(emptyMap())
                )
            )

        //start trade session
        marketDataBrokerGrpcStub.stubForGetCandles("get-candles.json")
        val startTradeSessionResponse = testRestTemplate.postForEntity(
            "/trade-sessions",
            StartTradeSessionRequest(
                strategyConfiguration.id!!,
                4,
                InstrumentDto("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
            ),
            StartTradeSessionResponse::class.java
        )
        startTradeSessionResponse.statusCode shouldBe HttpStatus.OK

        //income candle event
        usersBrokerGrpcStub.stubForGetAccounts("get-accounts.json")
        ordersBrokerGrpcStub.stubForPostBuyOrder("post-buy-order.json")
        ordersBrokerGrpcStub.stubForPostSellOrder("post-sell-order.json")
        telegramNotificationHttpStub.stubForSendNotification()

        tradeService.processIncomeCandle(
            ProcessIncomeCandleCommand(
                Candle(
                    interval = CandleInterval.ONE_MIN,
                    openPrice = BigDecimal(99),
                    closePrice = BigDecimal(100),
                    highestPrice = BigDecimal(100),
                    lowestPrice = BigDecimal(98),
                    volume = 10,
                    endTime = LocalDateTime.parse("2024-01-30T10:17:00"),
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
                )
            )
        )

        tradeService.processIncomeCandle(
            ProcessIncomeCandleCommand(
                Candle(
                    interval = CandleInterval.ONE_MIN,
                    openPrice = BigDecimal(100),
                    closePrice = BigDecimal(101),
                    highestPrice = BigDecimal(101),
                    lowestPrice = BigDecimal(99),
                    volume = 10,
                    endTime = LocalDateTime.parse("2024-01-30T10:18:00"),
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
                )
            )
        )

        tradeService.processIncomeCandle(
            ProcessIncomeCandleCommand(
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
            )
        )

        tradeService.processIncomeCandle(
            ProcessIncomeCandleCommand(
                Candle(
                    interval = CandleInterval.ONE_MIN,
                    openPrice = BigDecimal(102),
                    closePrice = BigDecimal(103),
                    highestPrice = BigDecimal(103),
                    lowestPrice = BigDecimal(101),
                    volume = 10,
                    endTime = LocalDateTime.parse("2024-01-30T10:20:00"),
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
                )
            )
        )

        tradeService.processIncomeCandle(
            ProcessIncomeCandleCommand(
                Candle(
                    interval = CandleInterval.ONE_MIN,
                    openPrice = BigDecimal(103),
                    closePrice = BigDecimal(106),
                    highestPrice = BigDecimal(106),
                    lowestPrice = BigDecimal(102),
                    volume = 10,
                    endTime = LocalDateTime.parse("2024-01-30T10:21:00"),
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1"
                )
            )
        )

        ordersBrokerGrpcStub.verifyForPostBuyOrder("post-buy-order.json")
        ordersBrokerGrpcStub.verifyForPostSellOrder("post-sell-order.json")

        //check orders
        val orders = tradeOrderRepository.findAll().toList()
        orders shouldHaveSize 2
        val buyOrder = orders[0]
        with(buyOrder) {
            ticker shouldBe "SBER"
            instrumentId shouldBe "e6123145-9665-43e0-8413-cd61b8aa9b1"
            date shouldBe LocalDateTime.parse("2024-01-30T10:15:30")
            lotsQuantity shouldBe 4
            totalPrice shouldBe BigDecimal("85.000000000")
            executedCommission shouldBe BigDecimal("1.000000000")
            direction shouldBe TradeDirection.BUY
            strategyConfigurationId.shouldNotBeNull()
        }
        val sellOrder = orders[1]
        with(sellOrder) {
            ticker shouldBe "SBER"
            instrumentId shouldBe "e6123145-9665-43e0-8413-cd61b8aa9b1"
            date shouldBe LocalDateTime.parse("2024-01-30T10:15:30")
            lotsQuantity shouldBe 4
            totalPrice shouldBe BigDecimal("89.000000000")
            executedCommission shouldBe BigDecimal("1.000000000")
            direction shouldBe TradeDirection.SELL
            strategyConfigurationId.shouldNotBeNull()
        }

        //check trade session
        val tradeSessions = tradeSessionCache.findAll()
        tradeSessions shouldHaveSize 1
        tradeSessions[0].status shouldBe TradeSessionStatus.WAITING
    }

})