package ru.kcheranev.trading.test.integration

import io.kotest.assertions.withClue
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.bigdecimal.shouldBeGreaterThan
import io.kotest.matchers.date.shouldNotBeBefore
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import ru.kcheranev.trading.core.port.common.model.ComparedField
import ru.kcheranev.trading.core.port.common.model.Comparsion
import ru.kcheranev.trading.core.port.common.model.Page
import ru.kcheranev.trading.core.port.common.model.Sort
import ru.kcheranev.trading.core.port.common.model.SortDirection
import ru.kcheranev.trading.domain.entity.TradeDirection
import ru.kcheranev.trading.domain.entity.TradeOrderSort
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyType
import ru.kcheranev.trading.infra.adapter.income.web.model.request.TradeOrderSearchRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.response.TradeOrderSearchResponse
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeOrderEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.TradeOrderRepository
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.TradeSessionRepository
import ru.kcheranev.trading.test.IntegrationTest
import java.math.BigDecimal
import java.time.LocalDateTime

@IntegrationTest
class SearchTradeOrderIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val strategyConfigurationRepository: StrategyConfigurationRepository,
    private val tradeSessionRepository: TradeSessionRepository,
    private val tradeOrderRepository: TradeOrderRepository,
    private val integrationTestExtensions: List<Extension>
) : StringSpec({

    extensions(integrationTestExtensions)

    beforeEach {
        val strategyConfiguration =
            strategyConfigurationRepository.save(
                StrategyConfigurationEntity(
                    null,
                    StrategyType.MOVING_MOMENTUM.name,
                    10,
                    CandleInterval.ONE_MIN,
                    MapWrapper(mapOf("param1" to "value1"))
                )
            )
        val tradeSession1 =
            tradeSessionRepository.save(
                TradeSessionEntity(
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    status = TradeSessionStatus.WAITING,
                    startDate = LocalDateTime.parse("2024-01-01T10:15:30"),
                    candleInterval = CandleInterval.ONE_MIN,
                    lotsQuantity = 10,
                    lastEventDate = LocalDateTime.parse("2024-01-01T10:16:10"),
                    strategyConfigurationId = strategyConfiguration.id!!
                )
            )
        val tradeOrders =
            listOf(
                TradeOrderEntity(
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    date = LocalDateTime.parse("2024-01-01T10:15:35"),
                    lotsQuantity = 10,
                    price = BigDecimal(100),
                    direction = TradeDirection.BUY,
                    tradeSessionId = tradeSession1.id!!
                ),
                TradeOrderEntity(
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    date = LocalDateTime.parse("2024-01-01T10:15:40"),
                    lotsQuantity = 11,
                    price = BigDecimal(100),
                    direction = TradeDirection.SELL,
                    tradeSessionId = tradeSession1.id!!
                ),
                TradeOrderEntity(
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    date = LocalDateTime.parse("2024-01-01T10:15:45"),
                    lotsQuantity = 12,
                    price = BigDecimal(110),
                    direction = TradeDirection.BUY,
                    tradeSessionId = tradeSession1.id!!
                ),
                TradeOrderEntity(
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    date = LocalDateTime.parse("2024-01-01T10:15:50"),
                    lotsQuantity = 13,
                    price = BigDecimal(120),
                    direction = TradeDirection.SELL,
                    tradeSessionId = tradeSession1.id!!
                ),
                TradeOrderEntity(
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    date = LocalDateTime.parse("2024-01-01T10:15:55"),
                    lotsQuantity = 14,
                    price = BigDecimal(130),
                    direction = TradeDirection.BUY,
                    tradeSessionId = tradeSession1.id!!
                ),
                TradeOrderEntity(
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    date = LocalDateTime.parse("2024-01-01T10:16:00"),
                    lotsQuantity = 15,
                    price = BigDecimal(140),
                    direction = TradeDirection.SELL,
                    tradeSessionId = tradeSession1.id!!
                ),
                TradeOrderEntity(
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    date = LocalDateTime.parse("2024-01-01T10:16:05"),
                    lotsQuantity = 16,
                    price = BigDecimal(150),
                    direction = TradeDirection.BUY,
                    tradeSessionId = tradeSession1.id!!
                )
            )
        tradeOrderRepository.saveAll(tradeOrders)

        val tradeSession2 =
            tradeSessionRepository.save(
                TradeSessionEntity(
                    ticker = "MOEX",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b2",
                    status = TradeSessionStatus.WAITING,
                    startDate = LocalDateTime.parse("2024-01-01T10:15:30"),
                    candleInterval = CandleInterval.ONE_MIN,
                    lotsQuantity = 10,
                    lastEventDate = LocalDateTime.parse("2024-01-01T10:16:10"),
                    strategyConfigurationId = strategyConfiguration.id!!
                )
            )
        tradeSessionRepository.save(tradeSession2)
        tradeOrderRepository.save(
            TradeOrderEntity(
                ticker = "MOEX",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b2",
                date = LocalDateTime.parse("2024-01-01T10:15:35"),
                lotsQuantity = 10,
                price = BigDecimal(100),
                direction = TradeDirection.BUY,
                tradeSessionId = tradeSession2.id!!
            )
        )
    }

    "should search trade orders" {
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            TradeOrderSearchRequest(),
            TradeOrderSearchResponse::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val tradeOrdersResult = response.body!!.tradeOrders
        tradeOrdersResult.size shouldBe 8
    }

    "should search trade orders by ticker" {
        //given
        val request =
            TradeOrderSearchRequest(
                ticker = "MOEX"
            )
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            request,
            TradeOrderSearchResponse::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val tradeOrdersResult = response.body!!.tradeOrders
        tradeOrdersResult.size shouldBe 1
        tradeOrdersResult.forEach {
            it.ticker shouldBe "MOEX"
        }
    }

    "should search trade orders by instrumentId" {
        //given
        val request =
            TradeOrderSearchRequest(
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b2"
            )
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            request,
            TradeOrderSearchResponse::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val tradeOrdersResult = response.body!!.tradeOrders
        tradeOrdersResult.size shouldBe 1
        tradeOrdersResult.forEach {
            it.instrumentId shouldBe "e6123145-9665-43e0-8413-cd61b8aa9b2"
        }
    }

    "should search trade orders by date" {
        //given
        val dateFilter = LocalDateTime.parse("2024-01-01T10:15:50")
        val request =
            TradeOrderSearchRequest(
                date = ComparedField(dateFilter, Comparsion.GT_EQ)
            )
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            request,
            TradeOrderSearchResponse::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val tradeOrdersResult = response.body!!.tradeOrders
        tradeOrdersResult.size shouldBe 4
        tradeOrdersResult.forEach {
            it.date shouldNotBeBefore dateFilter
        }
    }

    "should search trade orders by lotsQuantity" {
        //given
        val request =
            TradeOrderSearchRequest(
                lotsQuantity = ComparedField(13, Comparsion.LT_EQ)
            )
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            request,
            TradeOrderSearchResponse::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val tradeOrdersResult = response.body!!.tradeOrders
        tradeOrdersResult.size shouldBe 5
        tradeOrdersResult.forEach {
            it.lotsQuantity shouldBeLessThanOrEqual 13
        }
    }

    "should search trade orders by price" {
        //given
        val request =
            TradeOrderSearchRequest(
                price = ComparedField(BigDecimal(120), Comparsion.GT)
            )
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            request,
            TradeOrderSearchResponse::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val tradeOrdersResult = response.body!!.tradeOrders
        tradeOrdersResult.size shouldBe 3
        tradeOrdersResult.forEach {
            it.price shouldBeGreaterThan BigDecimal(120)
        }
    }

    "should search trade orders by direction" {
        //given
        val request =
            TradeOrderSearchRequest(
                direction = TradeDirection.BUY
            )
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            request,
            TradeOrderSearchResponse::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val tradeOrdersResult = response.body!!.tradeOrders
        tradeOrdersResult.size shouldBe 5
        tradeOrdersResult.forEach {
            it.direction shouldBe TradeDirection.BUY
        }
    }

    "should search trade orders by tradeSessionId" {
        //given
        val tradeSessionId = tradeSessionRepository.findAll().first { it.ticker == "MOEX" }.id
        val request =
            TradeOrderSearchRequest(
                tradeSessionId = tradeSessionId
            )
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            request,
            TradeOrderSearchResponse::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val tradeOrdersResult = response.body!!.tradeOrders
        tradeOrdersResult.size shouldBe 1
        tradeOrdersResult.forEach {
            it.tradeSessionId shouldBe tradeSessionId
        }
    }

    "should search trade orders with paging and sorting" {
        //given
        val request =
            TradeOrderSearchRequest(
                page = Page(2, 1),
                sort = Sort(TradeOrderSort.PRICE, SortDirection.DESC)
            )
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            request,
            TradeOrderSearchResponse::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val tradeOrdersResult = response.body!!.tradeOrders
        tradeOrdersResult.size shouldBe 2
        tradeOrdersResult[0].price = BigDecimal(140)
        tradeOrdersResult[1].price = BigDecimal(130)
    }

    "should return empty result when there are no trade orders found" {
        //given
        val request =
            TradeOrderSearchRequest(
                ticker = "ANY_ANOTHER_TICKER"
            )
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            request,
            TradeOrderSearchResponse::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val tradeOrdersResult = response.body!!.tradeOrders
        tradeOrdersResult.size shouldBe 0
    }

})