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
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.http.HttpStatus
import ru.kcheranev.trading.core.port.model.ComparedField
import ru.kcheranev.trading.core.port.model.Comparsion
import ru.kcheranev.trading.core.port.model.Page
import ru.kcheranev.trading.core.port.model.sort.Sort
import ru.kcheranev.trading.core.port.model.sort.SortDirection
import ru.kcheranev.trading.core.port.model.sort.TradeOrderSort
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.TradeDirection
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.request.SearchTradeOrderRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.response.TradeOrderSearchResponseDto
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeOrderEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import ru.kcheranev.trading.test.IntegrationTest
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@IntegrationTest
class SearchTradeOrderIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    beforeEach {
        val tradeSession1Id = UUID.fromString("e74e3deb-2630-43bb-a37b-a7b59cc66389")
        jdbcTemplate.insert(
            TradeSessionEntity(
                id = tradeSession1Id,
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.parse("2024-01-01T10:15:30"),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                lotsQuantityInPosition = 0,
                strategyType = "DUMMY",
                strategyParameters = MapWrapper(mapOf("paramName" to 1))
            )
        )
        val tradeSession2Id = UUID.randomUUID()
        jdbcTemplate.insert(
            TradeSessionEntity(
                id = tradeSession2Id,
                ticker = "MOEX",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b2",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.parse("2024-01-01T10:15:30"),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                lotsQuantityInPosition = 0,
                strategyType = "DUMMY",
                strategyParameters = MapWrapper(mapOf("paramName" to 1))
            )
        )
        val tradeOrders =
            listOf(
                TradeOrderEntity(
                    id = UUID.randomUUID(),
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    date = LocalDateTime.parse("2024-01-01T10:15:35"),
                    lotsQuantity = 10,
                    totalPrice = BigDecimal(100),
                    executedCommission = BigDecimal(1),
                    direction = TradeDirection.BUY,
                    tradeSessionId = tradeSession1Id
                ),
                TradeOrderEntity(
                    id = UUID.randomUUID(),
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    date = LocalDateTime.parse("2024-01-01T10:15:40"),
                    lotsQuantity = 11,
                    totalPrice = BigDecimal(100),
                    executedCommission = BigDecimal(2),
                    direction = TradeDirection.SELL,
                    tradeSessionId = tradeSession2Id
                ),
                TradeOrderEntity(
                    id = UUID.randomUUID(),
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    date = LocalDateTime.parse("2024-01-01T10:15:45"),
                    lotsQuantity = 12,
                    totalPrice = BigDecimal(110),
                    executedCommission = BigDecimal(3),
                    direction = TradeDirection.BUY,
                    tradeSessionId = tradeSession2Id
                ),
                TradeOrderEntity(
                    id = UUID.randomUUID(),
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    date = LocalDateTime.parse("2024-01-01T10:15:50"),
                    lotsQuantity = 13,
                    totalPrice = BigDecimal(120),
                    executedCommission = BigDecimal(4),
                    direction = TradeDirection.SELL,
                    tradeSessionId = tradeSession2Id
                ),
                TradeOrderEntity(
                    id = UUID.randomUUID(),
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    date = LocalDateTime.parse("2024-01-01T10:15:55"),
                    lotsQuantity = 14,
                    totalPrice = BigDecimal(130),
                    executedCommission = BigDecimal(5),
                    direction = TradeDirection.BUY,
                    tradeSessionId = tradeSession2Id
                ),
                TradeOrderEntity(
                    id = UUID.randomUUID(),
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    date = LocalDateTime.parse("2024-01-01T10:16:00"),
                    lotsQuantity = 15,
                    totalPrice = BigDecimal(140),
                    executedCommission = BigDecimal(6),
                    direction = TradeDirection.SELL,
                    tradeSessionId = tradeSession2Id
                ),
                TradeOrderEntity(
                    id = UUID.randomUUID(),
                    ticker = "SBER",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                    date = LocalDateTime.parse("2024-01-01T10:16:05"),
                    lotsQuantity = 16,
                    totalPrice = BigDecimal(150),
                    executedCommission = BigDecimal(7),
                    direction = TradeDirection.BUY,
                    tradeSessionId = tradeSession2Id
                ),
                TradeOrderEntity(
                    id = UUID.randomUUID(),
                    ticker = "MOEX",
                    instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b2",
                    date = LocalDateTime.parse("2024-01-01T10:15:35"),
                    lotsQuantity = 10,
                    totalPrice = BigDecimal(100),
                    executedCommission = BigDecimal(1),
                    direction = TradeDirection.BUY,
                    tradeSessionId = tradeSession2Id
                )
            )
        jdbcTemplate.insertAll(tradeOrders)
    }

    "should search trade orders" {
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            SearchTradeOrderRequestDto(),
            TradeOrderSearchResponseDto::class.java
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
            SearchTradeOrderRequestDto(
                ticker = "MOEX"
            )
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            request,
            TradeOrderSearchResponseDto::class.java
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
            SearchTradeOrderRequestDto(
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b2"
            )
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            request,
            TradeOrderSearchResponseDto::class.java
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
            SearchTradeOrderRequestDto(
                date = ComparedField(dateFilter, Comparsion.GT_EQ)
            )
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            request,
            TradeOrderSearchResponseDto::class.java
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
            SearchTradeOrderRequestDto(
                lotsQuantity = ComparedField(13, Comparsion.LT_EQ)
            )
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            request,
            TradeOrderSearchResponseDto::class.java
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

    "should search trade orders by total price" {
        //given
        val request =
            SearchTradeOrderRequestDto(
                totalPrice = ComparedField(BigDecimal(120), Comparsion.GT)
            )
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            request,
            TradeOrderSearchResponseDto::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val tradeOrdersResult = response.body!!.tradeOrders
        tradeOrdersResult.size shouldBe 3
        tradeOrdersResult.forEach {
            it.totalPrice shouldBeGreaterThan BigDecimal(120)
        }
    }

    "should search trade orders by direction" {
        //given
        val request =
            SearchTradeOrderRequestDto(
                direction = TradeDirection.BUY
            )
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            request,
            TradeOrderSearchResponseDto::class.java
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
        val request =
            SearchTradeOrderRequestDto(
                tradeSessionId = UUID.fromString("e74e3deb-2630-43bb-a37b-a7b59cc66389")
            )

        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            request,
            TradeOrderSearchResponseDto::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val tradeOrdersResult = response.body!!.tradeOrders
        tradeOrdersResult.size shouldBe 1
        tradeOrdersResult.forEach {
            it.tradeSessionId shouldBe UUID.fromString("e74e3deb-2630-43bb-a37b-a7b59cc66389")
        }
    }

    "should search trade orders with paging and sorting" {
        //given
        val request =
            SearchTradeOrderRequestDto(
                page = Page(2, 1),
                sort = Sort(TradeOrderSort.TOTAL_PRICE, SortDirection.DESC)
            )
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            request,
            TradeOrderSearchResponseDto::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val tradeOrdersResult = response.body!!.tradeOrders
        tradeOrdersResult.size shouldBe 2
        tradeOrdersResult[0].totalPrice = BigDecimal(140)
        tradeOrdersResult[1].totalPrice = BigDecimal(130)
    }

    "should return empty result when there are no trade orders found" {
        //given
        val request =
            SearchTradeOrderRequestDto(
                ticker = "ANY_ANOTHER_TICKER"
            )
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-orders/search",
            request,
            TradeOrderSearchResponseDto::class.java
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