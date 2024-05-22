package ru.kcheranev.trading.test.integration

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.impl.TradeSessionCache
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository
import ru.kcheranev.trading.test.IntegrationTest
import ru.kcheranev.trading.test.stub.grpc.MarketDataBrokerGrpcStub
import ru.kcheranev.trading.test.util.MarketDataSubscriptionInitializer
import java.time.LocalDateTime
import java.util.UUID

@IntegrationTest
class StopTradeSessionIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val tradeSessionCache: TradeSessionCache,
    private val strategyConfigurationRepository: StrategyConfigurationRepository,
    private val marketDataSubscriptionInitializer: MarketDataSubscriptionInitializer,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    val testName = "stop-trade-session"

    val marketDataBrokerGrpcStub = MarketDataBrokerGrpcStub(testName)

    "should stop trade session" {
        //given
        val strategyConfiguration =
            strategyConfigurationRepository.save(
                StrategyConfigurationEntity(
                    null,
                    "DUMMY_LONG",
                    CandleInterval.ONE_MIN,
                    MapWrapper(mapOf("param1" to 1))
                )
            )
        val tradeSessionId = UUID.randomUUID()
        tradeSessionCache.put(
            tradeSessionId,
            TradeSession(
                id = TradeSessionId(tradeSessionId),
                ticker = "SBER",
                instrumentId = "e6123145-9665-43e0-8413-cd61b8aa9b1",
                status = TradeSessionStatus.WAITING,
                startDate = LocalDateTime.parse("2024-01-01T10:15:30"),
                candleInterval = CandleInterval.ONE_MIN,
                lotsQuantity = 10,
                strategy = mockk(),
                strategyConfigurationId = StrategyConfigurationId(strategyConfiguration.id!!)
            )
        )
        marketDataSubscriptionInitializer.init("SBER", CandleInterval.ONE_MIN)

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

        val tradeSessionList = tradeSessionCache.findAll()
        tradeSessionList.size shouldBe 0
    }

})