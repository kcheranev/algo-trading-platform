package com.github.trading.test.integration

import com.github.trading.core.strategy.lotsquantity.LOTS_QUANTITY_STRATEGY_PARAMETER_NAME
import com.github.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyType
import com.github.trading.domain.entity.TradeSessionStatus
import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.Instrument
import com.github.trading.infra.adapter.income.web.rest.model.common.InstrumentDto
import com.github.trading.infra.adapter.income.web.rest.model.request.CreateTradeSessionRequestDto
import com.github.trading.infra.adapter.income.web.rest.model.response.CreateTradeSessionResponseDto
import com.github.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionCacheHolder
import com.github.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import com.github.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import com.github.trading.infra.adapter.outcome.persistence.impl.TradeStrategyCache
import com.github.trading.infra.adapter.outcome.persistence.model.MapWrapper
import com.github.trading.test.IntegrationTest
import com.github.trading.test.stub.grpc.MarketDataBrokerGrpcStub
import io.kotest.assertions.withClue
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.http.HttpStatus
import java.util.UUID

@IntegrationTest
class CreateTradeSessionIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val candleSubscriptionCacheHolder: CandleSubscriptionCacheHolder,
    private val tradeSessionCache: TradeStrategyCache,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    val testName = "create-trade-session"

    val marketDataBrokerGrpcStub = MarketDataBrokerGrpcStub(testName)

    "should create trade session" {
        //given
        val strategyConfigurationId = UUID.randomUUID()
        val strategyConfiguration =
            jdbcTemplate.insert(
                StrategyConfigurationEntity(
                    id = strategyConfigurationId,
                    name = "dummy",
                    type = "DUMMY_LONG",
                    candleInterval = CandleInterval.ONE_MIN,
                    parameters = MapWrapper(mapOf("param1" to 1, LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 1))
                )
            )
        marketDataBrokerGrpcStub.stubForGetCandles("get-candles.json")

        //when
        val response = testRestTemplate.postForEntity(
            "/trade-sessions",
            CreateTradeSessionRequestDto(
                strategyConfiguration.id,
                OrderLotsQuantityStrategyType.HARDCODED,
                InstrumentDto("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
            ),
            CreateTradeSessionResponseDto::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("trade session id should be present") {
            response.body?.tradeSessionId shouldNotBe null
        }

        marketDataBrokerGrpcStub.verifyForGetCandles("get-candles.json")
        marketDataBrokerGrpcStub.verifyForMarketDataStream("market-data-stream-subscribe.json")

        val tradeSessions = jdbcTemplate.findAll(TradeSessionEntity::class.java)
        tradeSessions shouldHaveSize 1
        val tradeSession = tradeSessions.first()
        tradeSession.ticker shouldBe "SBER"
        tradeSession.instrumentId shouldBe "e6123145-9665-43e0-8413-cd61b8aa9b1"
        tradeSession.status shouldBe TradeSessionStatus.WAITING
        tradeSession.candleInterval shouldBe CandleInterval.ONE_MIN
        tradeSession.orderLotsQuantityStrategyType shouldBe OrderLotsQuantityStrategyType.HARDCODED
        tradeSession.strategyParameters.value shouldBe mapOf("param1" to 1, LOTS_QUANTITY_STRATEGY_PARAMETER_NAME to 1)

        val tradeStrategies = tradeSessionCache.tradeStrategies
        tradeStrategies shouldHaveSize 1
        tradeStrategies shouldContainKey tradeSession.id

        val candleSubscriptions = candleSubscriptionCacheHolder.findAll()
        candleSubscriptions shouldHaveSize 1
        val candleSubscription = candleSubscriptions.first()
        candleSubscription.instrument shouldBe Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
        candleSubscription.candleInterval shouldBe CandleInterval.ONE_MIN
    }

})