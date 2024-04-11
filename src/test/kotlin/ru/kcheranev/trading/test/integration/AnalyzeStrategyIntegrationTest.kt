package ru.kcheranev.trading.test.integration

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.TradeDirection
import ru.kcheranev.trading.infra.adapter.income.web.model.request.InstrumentDto
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StrategyAnalyzeRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.model.response.PeriodStrategyAnalyzeResultDto
import ru.kcheranev.trading.test.IntegrationTest
import ru.kcheranev.trading.test.stub.grpc.MarketDataBrokerGrpcStub
import java.math.BigDecimal
import java.time.LocalDate
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
            "/backtesting/analyze",
            StrategyAnalyzeRequestDto(
                strategyType = "DUMMY",
                strategyParams = emptyMap(),
                instrument = InstrumentDto("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                candleInterval = CandleInterval.ONE_MIN,
                candlesFrom = LocalDateTime.parse("2024-01-30T10:12:00"),
                candlesTo = LocalDateTime.parse("2024-01-30T10:26:00")
            ),
            PeriodStrategyAnalyzeResultDto::class.java
        )
        strategyAnalyzeResponse.statusCode shouldBe HttpStatus.OK
        val periodAnalyzeResult = strategyAnalyzeResponse.body
        periodAnalyzeResult.shouldNotBeNull()
        periodAnalyzeResult.results shouldHaveSize 1
        periodAnalyzeResult.results.keys.first() shouldBe LocalDate.parse("2024-01-30")
        periodAnalyzeResult.totalGrossProfit shouldBe BigDecimal("2.000000000")
        periodAnalyzeResult.totalNetProfit shouldBe BigDecimal("1.83680000000000")
        with(periodAnalyzeResult.results.values.first()) {
            averageLoss shouldBe BigDecimal("-7.000000000")
            averageProfit shouldBe BigDecimal("9.000000000")
            netLoss shouldBe BigDecimal("-7.07880000000000")
            grossLoss shouldBe BigDecimal("-7.000000000")
            numberOfBars shouldBe 6
            numberOfConsecutiveProfitPositions shouldBe 1
            numberOfConsecutiveProfitPositions shouldBe 1
            numberOfLosingPositions shouldBe 1
            numberOfProfitPositions shouldBe 1
            netProfit shouldBe BigDecimal("8.91560000000000")
            grossProfit shouldBe BigDecimal("9.000000000")
            profitLoss shouldBe BigDecimal("1.83680000000000")
            trades shouldHaveSize 2
            totalGrossProfit shouldBe BigDecimal("2.000000000")
            totalNetProfit shouldBe BigDecimal("1.83680000000000")
            with(trades[0]) {
                entry.date shouldBe LocalDateTime.parse("2024-01-30T10:17")
                entry.direction shouldBe TradeDirection.BUY
                entry.grossPrice shouldBe BigDecimal("101.000000000")
                entry.netPrice shouldBe BigDecimal("101.04040000000000")
                exit.date shouldBe LocalDateTime.parse("2024-01-30T10:19")
                exit.direction shouldBe TradeDirection.SELL
                exit.grossPrice shouldBe BigDecimal("110.000000000")
                exit.netPrice shouldBe BigDecimal("109.95600000000000")
                grossProfit shouldBe BigDecimal("9.000000000")
                netProfit shouldBe BigDecimal("8.91560000000000")
            }
            with(trades[1]) {
                entry.date shouldBe LocalDateTime.parse("2024-01-30T10:23")
                entry.direction shouldBe TradeDirection.BUY
                entry.grossPrice shouldBe BigDecimal("102.000000000")
                entry.netPrice shouldBe BigDecimal("102.04080000000000")
                exit.date shouldBe LocalDateTime.parse("2024-01-30T10:25")
                exit.direction shouldBe TradeDirection.SELL
                exit.grossPrice shouldBe BigDecimal("95.000000000")
                exit.netPrice shouldBe BigDecimal("94.96200000000000")
                grossProfit shouldBe BigDecimal("-7.000000000")
                netProfit shouldBe BigDecimal("-7.07880000000000")
            }
        }
    }

})