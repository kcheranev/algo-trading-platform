package ru.kcheranev.trading.test.integration

import com.github.tomakehurst.wiremock.WireMockServer
import io.kotest.assertions.withClue
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionCounter
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.impl.TradeStrategyCache
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.TradeSessionRepository
import ru.kcheranev.trading.infra.config.BrokerApi
import ru.kcheranev.trading.test.IntegrationTest
import ru.kcheranev.trading.test.stub.grpc.MarketDataBrokerGrpcStub
import java.time.LocalDateTime

@IntegrationTest
class StopTradeSessionIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val tradeSessionRepository: TradeSessionRepository,
    private val strategyConfigurationRepository: StrategyConfigurationRepository,
    private val grpcWireMockServer: WireMockServer,
    private val tradeStrategyCache: TradeStrategyCache,
    private val candleSubscriptionCounter: CandleSubscriptionCounter,
    private val brokerApi: BrokerApi,
    private val integrationTestExtensions: List<Extension>
) : StringSpec({

    extensions(integrationTestExtensions)

    val testName = "stop-trade-session"

    val marketDataBrokerGrpcStub by lazy { MarketDataBrokerGrpcStub(testName, grpcWireMockServer) }

    val marketDataStreamService = brokerApi.marketDataStreamService

    "should stop trade session" {
        //given
        val strategyConfiguration =
            strategyConfigurationRepository.save(
                StrategyConfigurationEntity(
                    null,
                    "DUMMY",
                    10,
                    CandleInterval.ONE_MIN,
                    MapWrapper(mapOf("param1" to "value1"))
                )
            )
        val tradeSession =
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
        tradeStrategyCache.put(tradeSession.id!!, mockk())
        candleSubscriptionCounter.addCandleSubscription("candles_SBER_ONE_MIN")
        marketDataStreamService.newStream("candles_SBER_ONE_MIN", {}) {}

        //when
        val response = testRestTemplate.exchange(
            "/trade-sessions/${tradeSession.id}/stop",
            HttpMethod.POST,
            HttpEntity.EMPTY,
            Unit::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK

        marketDataBrokerGrpcStub.verifyForMarketDataStream("market-data-stream-unsubscribe.json")

        val tradeSessionList = tradeSessionRepository.findAll().toList()
        tradeSessionList.size shouldBe 1
        val stopperTradeSession = tradeSessionList[0]
        stopperTradeSession.ticker shouldBe "SBER"
        stopperTradeSession.instrumentId shouldBe "e6123145-9665-43e0-8413-cd61b8aa9b1"
        stopperTradeSession.status shouldBe TradeSessionStatus.STOPPED
        withClue("start date should be present") {
            stopperTradeSession.startDate shouldNotBe null
        }
        stopperTradeSession.candleInterval shouldBe CandleInterval.ONE_MIN
        stopperTradeSession.lotsQuantity shouldBe 10
        withClue("last event date should not be null") {
            stopperTradeSession.lastEventDate shouldNotBe null
        }
    }

})