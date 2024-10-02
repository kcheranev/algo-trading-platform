package ru.kcheranev.trading.test.integration

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.TradeDirection
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.common.InstrumentDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.request.StrategyAnalyzeRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.response.StrategyAnalyzeResponseDto
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

        //when
        val strategyAnalyzeResponse = testRestTemplate.postForEntity(
            "/backtesting/analyze",
            StrategyAnalyzeRequestDto(
                strategyType = "DUMMY_LONG",
                strategyParameters = emptyMap(),
                instrument = InstrumentDto("e6123145-9665-43e0-8413-cd61b8aa9b1", "SBER"),
                candleInterval = CandleInterval.ONE_MIN,
                from = LocalDate.parse("2024-01-30"),
                to = LocalDate.parse("2024-01-30")
            ),
            StrategyAnalyzeResponseDto::class.java
        )

        //then
        strategyAnalyzeResponse.statusCode shouldBe HttpStatus.OK
        val strategyAnalyzeResult = strategyAnalyzeResponse.body?.analyzeResult
        strategyAnalyzeResult.shouldNotBeNull()
        strategyAnalyzeResult.results shouldHaveSize 1
        strategyAnalyzeResult.results.keys.first() shouldBe LocalDate.parse("2024-01-30")
        strategyAnalyzeResult.grossValue shouldBe BigDecimal("2.000000000")
        strategyAnalyzeResult.netValue shouldBe BigDecimal("1.83680000000000")
        strategyAnalyzeResult.profitPositionsCount shouldBe 1
        strategyAnalyzeResult.losingPositionsCount shouldBe 1
        strategyAnalyzeResult.profitLossPositionsRatio shouldBe BigDecimal("1.00000")
        with(strategyAnalyzeResult.results.values.first()) {
            positionsCount shouldBe 3
            netProfit shouldBe BigDecimal("8.91560000000000")
            grossProfit shouldBe BigDecimal("9.000000000")
            netLoss shouldBe BigDecimal("-7.07880000000000")
            grossLoss shouldBe BigDecimal("-7.000000000")
            losingPositionsCount shouldBe 1
            profitPositionsCount shouldBe 1
            trades shouldHaveSize 3
            netValue shouldBe BigDecimal("1.83680000000000")
            grossValue shouldBe BigDecimal("2.000000000")
            with(trades[0]) {
                netValue shouldBe BigDecimal("8.91560000000000")
                grossValue shouldBe BigDecimal("9.000000000")
                netProfit shouldBe BigDecimal("8.91560000000000")
                grossProfit shouldBe BigDecimal("9.000000000")
                netLoss shouldBe BigDecimal.ZERO
                grossLoss shouldBe BigDecimal.ZERO
                entry.date shouldBe LocalDateTime.parse("2024-01-30T10:18")
                entry.direction shouldBe TradeDirection.BUY
                entry.grossPrice shouldBe BigDecimal("101.000000000")
                entry.netPrice shouldBe BigDecimal("101.04040000000000")
                exit?.date shouldBe LocalDateTime.parse("2024-01-30T10:20")
                exit?.direction shouldBe TradeDirection.SELL
                exit?.grossPrice shouldBe BigDecimal("110.000000000")
                exit?.netPrice shouldBe BigDecimal("109.95600000000000")
            }
            with(trades[1]) {
                netValue shouldBe BigDecimal("-7.07880000000000")
                grossValue shouldBe BigDecimal("-7.000000000")
                netProfit shouldBe BigDecimal.ZERO
                grossProfit shouldBe BigDecimal.ZERO
                netLoss shouldBe BigDecimal("-7.07880000000000")
                grossLoss shouldBe BigDecimal("-7.000000000")
                entry.date shouldBe LocalDateTime.parse("2024-01-30T10:24")
                entry.direction shouldBe TradeDirection.BUY
                entry.grossPrice shouldBe BigDecimal("102.000000000")
                entry.netPrice shouldBe BigDecimal("102.04080000000000")
                exit?.date shouldBe LocalDateTime.parse("2024-01-30T10:26")
                exit?.direction shouldBe TradeDirection.SELL
                exit?.grossPrice shouldBe BigDecimal("95.000000000")
                exit?.netPrice shouldBe BigDecimal("94.96200000000000")
            }
            with(trades[2]) {
                netLoss shouldBe BigDecimal.ZERO
                grossValue shouldBe BigDecimal.ZERO
                netProfit shouldBe BigDecimal.ZERO
                grossProfit shouldBe BigDecimal.ZERO
                netLoss shouldBe BigDecimal.ZERO
                grossLoss shouldBe BigDecimal.ZERO
                entry.date shouldBe LocalDateTime.parse("2024-01-30T10:28")
                entry.direction shouldBe TradeDirection.BUY
                entry.grossPrice shouldBe BigDecimal("102.000000000")
                entry.netPrice shouldBe BigDecimal("102.04080000000000")
                exit.shouldBeNull()
            }
        }
    }

})