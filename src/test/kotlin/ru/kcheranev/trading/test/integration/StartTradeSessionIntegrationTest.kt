package ru.kcheranev.trading.test.integration

import io.kotest.assertions.withClue
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.income.web.model.request.InstrumentDto
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StartTradeSessionRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.model.response.StartTradeSessionResponseDto
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.impl.TradeSessionCache
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.StrategyConfigurationRepository
import ru.kcheranev.trading.test.IntegrationTest
import ru.kcheranev.trading.test.stub.grpc.MarketDataBrokerGrpcStub

@IntegrationTest
class StartTradeSessionIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val tradeSessionCache: TradeSessionCache,
    private val strategyConfigurationRepository: StrategyConfigurationRepository,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    val testName = "start-trade-session"

    val marketDataBrokerGrpcStub = MarketDataBrokerGrpcStub(testName)

    "should start trade session" {
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
        marketDataBrokerGrpcStub.stubForGetCandles("get-candles.json")

        //when
        val response = testRestTemplate.postForEntity(
            "/trade-sessions",
            StartTradeSessionRequestDto(
                strategyConfiguration.id!!,
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

        val tradeSessionList = tradeSessionCache.findAll()
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
    }

})