package ru.kcheranev.trading.test.integration

import com.github.tomakehurst.wiremock.WireMockServer
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.extensions.wiremock.WireMockListener
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StartTradeSessionRequest
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.TradeSessionRepository
import ru.kcheranev.trading.test.IntegrationTest
import ru.kcheranev.trading.test.extension.CleanDatabaseExtension
import ru.kcheranev.trading.test.stub.grpc.MarketDataBrokerGrpcStub

@IntegrationTest
class StopTradeSessionIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val tradeSessionRepository: TradeSessionRepository,
    private val strategyConfigurationRepository: StrategyConfigurationRepository,
    private val cleanDatabaseExtension: CleanDatabaseExtension,
    private val grpcWireMockListener: WireMockListener,
    private val grpcWireMockServer: WireMockServer
) : StringSpec({

    val testName = "stop-trade-session"

    val marketDataBrokerGrpcStub by lazy { MarketDataBrokerGrpcStub(testName, grpcWireMockServer) }

    listener(grpcWireMockListener)

    extension(cleanDatabaseExtension)

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
        marketDataBrokerGrpcStub.stubForGetCandles("get-candles.json")
        testRestTemplate.postForEntity(
            "/trade-sessions",
            StartTradeSessionRequest(
                strategyConfiguration.id!!,
                4,
                Instrument("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER")
            ),
            Unit::class.java
        )
        val tradeSessionId = tradeSessionRepository.findAll().first().id

        //when
        val response = testRestTemplate.exchange(
            "/trade-sessions/$tradeSessionId/stop",
            HttpMethod.POST,
            HttpEntity.EMPTY,
            Unit::class.java
        )

        //then
        response.statusCode shouldBe HttpStatus.OK

        marketDataBrokerGrpcStub.verifyForMarketDataStream("market-data-stream-subscribe.json")
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
        stopperTradeSession.lotsQuantity shouldBe 4
        withClue("last event date should not be null") {
            stopperTradeSession.lastEventDate shouldNotBe null
        }
    }

})