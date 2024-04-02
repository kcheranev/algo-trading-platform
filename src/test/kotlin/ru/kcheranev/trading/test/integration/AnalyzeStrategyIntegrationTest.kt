package ru.kcheranev.trading.test.integration

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.income.web.model.request.InstrumentDto
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StrategyAnalyzeRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.response.StrategyAnalyzeResponse
import ru.kcheranev.trading.test.IntegrationTest
import ru.kcheranev.trading.test.stub.grpc.MarketDataBrokerGrpcStub
import java.math.BigDecimal
import java.time.LocalDateTime

@IntegrationTest
class AnalyzeStrategyIntegrationTest(
    private val testRestTemplate: TestRestTemplate,
    private val resetTestContextExtensions: List<Extension>
) : StringSpec({

    extensions(resetTestContextExtensions)

    val testName = "analyze-trade-strategy"

    val marketDataBrokerGrpcStub = MarketDataBrokerGrpcStub(testName)

    "should analyze trade strategy" {
        //given
        marketDataBrokerGrpcStub.stubForGetCandles("get-candles.json")
        val strategyAnalyzeResponse = testRestTemplate.postForEntity(
            "/backtesting",
            StrategyAnalyzeRequest(
                strategyType = "DUMMY",
                strategyParams = emptyMap(),
                instrument = InstrumentDto("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                candleInterval = CandleInterval.ONE_MIN,
                candlesFrom = LocalDateTime.parse("2024-01-30T10:12:00"),
                candlesTo = LocalDateTime.parse("2024-01-30T10:26:00")
            ),
            StrategyAnalyzeResponse::class.java
        )
        strategyAnalyzeResponse.statusCode shouldBe HttpStatus.OK
        with(strategyAnalyzeResponse.body!!) {
            averageLoss shouldBe BigDecimal("-7.000000000")
            averageProfit shouldBe BigDecimal("9.000000000")
            netLoss shouldBe BigDecimal("-7.000000000")
            grossLoss shouldBe BigDecimal("-7.000000000")
            numberOfBars shouldBe 6
            numberOfConsecutiveProfitPositions shouldBe 1
            numberOfConsecutiveProfitPositions shouldBe 1
            numberOfLosingPositions shouldBe 1
            numberOfProfitPositions shouldBe 1
            netProfit shouldBe BigDecimal("9.000000000")
            grossProfit shouldBe BigDecimal("9.000000000")
            profitLoss shouldBe BigDecimal("2.000000000")
        }
    }

})