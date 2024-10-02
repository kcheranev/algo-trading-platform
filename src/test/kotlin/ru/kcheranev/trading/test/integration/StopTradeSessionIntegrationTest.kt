package ru.kcheranev.trading.test.integration

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionHolder
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.impl.TradeStrategyCache
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import ru.kcheranev.trading.test.IntegrationTest
import ru.kcheranev.trading.test.stub.grpc.MarketDataBrokerGrpcStub
import ru.kcheranev.trading.test.util.MarketDataSubscriptionInitializer
import java.time.LocalDateTime
import java.util.UUID

@IntegrationTest
class StopTradeSessionIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val tradeStrategyCache: TradeStrategyCache,
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val marketDataSubscriptionInitializer: MarketDataSubscriptionInitializer,
    private val candleSubscriptionHolder: CandleSubscriptionHolder,
    private val tradeSessionCache: TradeStrategyCache,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    val testName = "stop-trade-session"

    val marketDataBrokerGrpcStub = MarketDataBrokerGrpcStub(testName)

    "should stop trade session" {
        //given
        val tradeSessionId = UUID.randomUUID()
        jdbcTemplate.insert(
            TradeSessionEntity(
                id = tradeSessionId,
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
        tradeStrategyCache.put(tradeSessionId, mockk())
        marketDataSubscriptionInitializer.init(
            Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
            CandleInterval.ONE_MIN
        )

        //when
        val response = testRestTemplate.exchange(
            "/trade-sessions/${tradeSessionId}/stop",
            HttpMethod.POST,
            HttpEntity.EMPTY,
            Unit::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK

        marketDataBrokerGrpcStub.verifyForMarketDataStream("market-data-stream-unsubscribe.json")

        val tradeSessionList = jdbcTemplate.findAll(TradeSessionEntity::class.java)
        tradeSessionList.shouldHaveSize(1)
        val tradeSession = tradeSessionList.first()
        tradeSession.status shouldBe TradeSessionStatus.STOPPED

        tradeSessionCache.tradeStrategies.shouldBeEmpty()
        candleSubscriptionHolder.getSubscriptions().shouldBeEmpty()
    }

})