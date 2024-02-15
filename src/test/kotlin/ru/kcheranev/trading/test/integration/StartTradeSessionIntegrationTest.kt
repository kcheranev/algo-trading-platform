package ru.kcheranev.trading.test.integration

import com.github.tomakehurst.wiremock.WireMockServer
import io.kotest.assertions.withClue
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StartTradeSessionRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.response.StartTradeSessionResponse
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.TradeSessionRepository
import ru.kcheranev.trading.test.IntegrationTest
import ru.kcheranev.trading.test.stub.grpc.MarketDataBrokerGrpcStub

@IntegrationTest
class StartTradeSessionIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val tradeSessionRepository: TradeSessionRepository,
    private val strategyConfigurationRepository: StrategyConfigurationRepository,
    private val grpcWireMockServer: WireMockServer,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    val testName = "start-trade-session"

    val marketDataBrokerGrpcStub by lazy { MarketDataBrokerGrpcStub(testName, grpcWireMockServer) }

    "should start trade session" {
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
        marketDataBrokerGrpcStub.stubForGetCandles("get-candles.json")

        //when
        val response = testRestTemplate.postForEntity(
            "/trade-sessions",
            StartTradeSessionRequest(
                strategyConfiguration.id!!,
                4,
                Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
            ),
            StartTradeSessionResponse::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK
        withClue("trade session id should be present") {
            response.body?.tradeSessionId shouldNotBe null
        }

        marketDataBrokerGrpcStub.verifyForGetCandles("get-candles.json")
        marketDataBrokerGrpcStub.verifyForMarketDataStream("market-data-stream-subscribe.json")

        val tradeSessionList = tradeSessionRepository.findAll().toList()
        tradeSessionList.size shouldBe 1
        val tradeSession = tradeSessionList[0]
        tradeSession.ticker shouldBe "SBER"
        tradeSession.instrumentId shouldBe "e6123145-9665-43e0-8413-cd61b8aa9b1"
        tradeSession.status shouldBe TradeSessionStatus.WAITING
        withClue("start date should be present") {
            tradeSession.startDate shouldNotBe null
        }
        tradeSession.candleInterval shouldBe CandleInterval.ONE_MIN
        tradeSession.lotsQuantity shouldBe 4
        withClue("last event date should not be null") {
            tradeSession.lastEventDate shouldNotBe null
        }
    }

})