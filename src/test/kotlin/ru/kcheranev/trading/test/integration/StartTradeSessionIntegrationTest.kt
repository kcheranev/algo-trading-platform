package ru.kcheranev.trading.test.integration

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
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.common.InstrumentDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.request.StartTradeSessionRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.response.StartTradeSessionResponseDto
import ru.kcheranev.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionHolder
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.impl.TradeStrategyCache
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import ru.kcheranev.trading.test.IntegrationTest
import ru.kcheranev.trading.test.stub.grpc.MarketDataBrokerGrpcStub
import java.util.UUID

@IntegrationTest
class StartTradeSessionIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val candleSubscriptionHolder: CandleSubscriptionHolder,
    private val tradeSessionCache: TradeStrategyCache,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    val testName = "start-trade-session"

    val marketDataBrokerGrpcStub = MarketDataBrokerGrpcStub(testName)

    "should start trade session" {
        //given
        val strategyConfigurationId = UUID.randomUUID()
        val strategyConfiguration =
            jdbcTemplate.insert(
                StrategyConfigurationEntity(
                    id = strategyConfigurationId,
                    name = "dummy",
                    type = "DUMMY_LONG",
                    candleInterval = CandleInterval.ONE_MIN,
                    parameters = MapWrapper(mapOf("param1" to 1))
                )
            )
        marketDataBrokerGrpcStub.stubForGetCandles("get-candles.json")

        //when
        val response = testRestTemplate.postForEntity(
            "/trade-sessions",
            StartTradeSessionRequestDto(
                strategyConfiguration.id,
                4,
                InstrumentDto("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
            ),
            StartTradeSessionResponseDto::class.java
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
        withClue("start date should be present") {
            tradeSession.startDate shouldNotBe null
        }
        tradeSession.candleInterval shouldBe CandleInterval.ONE_MIN
        tradeSession.lotsQuantity shouldBe 4

        val tradeStrategies = tradeSessionCache.tradeStrategies
        tradeStrategies shouldHaveSize 1
        tradeStrategies shouldContainKey tradeSession.id

        val candleSubscriptions = candleSubscriptionHolder.getSubscriptions()
        candleSubscriptions shouldHaveSize 1
        val candleSubscription = candleSubscriptions.first()
        candleSubscription.instrument shouldBe Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
        candleSubscription.candleInterval shouldBe CandleInterval.ONE_MIN
        candleSubscription.subscriptionCount shouldBe 1
    }

})