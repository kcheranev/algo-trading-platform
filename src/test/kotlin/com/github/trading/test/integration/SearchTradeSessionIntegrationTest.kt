package com.github.trading.test.integration

import com.github.trading.core.strategy.lotsquantity.LOTS_QUANTITY_STRATEGY_PARAMETER_NAME
import com.github.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyType
import com.github.trading.domain.entity.TradeSessionStatus
import com.github.trading.domain.model.CandleInterval
import com.github.trading.infra.adapter.income.web.rest.model.request.SearchTradeSessionRequestDto
import com.github.trading.infra.adapter.income.web.rest.model.response.TradeSessionSearchResponseDto
import com.github.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import com.github.trading.infra.adapter.outcome.persistence.impl.TradeStrategyCache
import com.github.trading.infra.adapter.outcome.persistence.model.MapWrapper
import com.github.trading.test.IntegrationTest
import io.kotest.assertions.withClue
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.util.UUID

@IntegrationTest
class SearchTradeSessionIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val tradeStrategyCache: TradeStrategyCache,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    beforeEach {
        val tradeSession1Id = UUID.randomUUID()
        jdbcTemplate.insert(
            TradeSessionEntity(
                id = tradeSession1Id,
                ticker = "ABRD",
                instrumentId = "926fdfbf-4b07-47c9-8928-f49858ca33f2",
                status = TradeSessionStatus.WAITING,
                candleInterval = CandleInterval.ONE_MIN,
                orderLotsQuantityStrategyType = OrderLotsQuantityStrategyType.HARDCODED,
                positionLotsQuantity = 0,
                positionAveragePrice = BigDecimal.ZERO,
                strategyType = "DUMMY_LONG",
                strategyParameters = MapWrapper(mapOf("paramName" to 1, LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10))
            )
        )
        tradeStrategyCache.put(tradeSession1Id, mockk())
        val tradeSession2Id = UUID.randomUUID()
        jdbcTemplate.insert(
            TradeSessionEntity(
                id = tradeSession2Id,
                ticker = "MOEX",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b2",
                status = TradeSessionStatus.WAITING,
                candleInterval = CandleInterval.ONE_MIN,
                orderLotsQuantityStrategyType = OrderLotsQuantityStrategyType.HARDCODED,
                positionLotsQuantity = 0,
                positionAveragePrice = BigDecimal.ZERO,
                strategyType = "DUMMY_LONG",
                strategyParameters = MapWrapper(mapOf("paramName" to 1, LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 10))
            )
        )
        tradeStrategyCache.put(tradeSession2Id, mockk())
    }

    "should search trade sessions" {
        //when
        val response = testRestTemplate.postForEntity(
            "/trade-sessions/search",
            SearchTradeSessionRequestDto(),
            TradeSessionSearchResponseDto::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("Body should be present") {
            response.body shouldNotBe null
        }
        val tradeSessionsResult = response.body!!.tradeSessions
        tradeSessionsResult.size shouldBe 2
    }

})