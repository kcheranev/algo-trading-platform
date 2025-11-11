package com.github.trading.test.integration

import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.TradeDirection
import com.github.trading.domain.model.backtesting.ProfitTypeSort
import com.github.trading.infra.adapter.income.web.rest.model.common.InstrumentDto
import com.github.trading.infra.adapter.income.web.rest.model.request.StrategyAnalyzeRequestDto
import com.github.trading.infra.adapter.income.web.rest.model.request.StrategyAnalyzeResultFilterDto
import com.github.trading.infra.adapter.income.web.rest.model.request.StrategyParametersMutationDto
import com.github.trading.infra.adapter.income.web.rest.model.response.StrategyAnalyzeResponseDto
import com.github.trading.test.IntegrationTest
import com.github.trading.test.stub.grpc.MarketDataBrokerGrpcStub
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

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
            "/backtesting",
            StrategyAnalyzeRequestDto(
                strategyType = "DUMMY_LONG",
                strategyParameters = emptyMap(),
                mutableStrategyParameters = emptyMap(),
                parametersMutation = StrategyParametersMutationDto(
                    divisionFactor = BigDecimal(2),
                    variantsCount = 5,
                ),
                resultFilter = StrategyAnalyzeResultFilterDto(
                    resultsLimit = 1,
                    minProfitLossTradesRatio = BigDecimal(1),
                    tradesByDayCountFactor = BigDecimal(1)
                ),
                profitTypeSort = ProfitTypeSort.GROSS,
                instrument = InstrumentDto("926fdfbf-4b07-47c9-8928-f49858ca33f2", "ABRD"),
                candleInterval = CandleInterval.ONE_MIN,
                from = LocalDate.parse("2024-01-30"),
                to = LocalDate.parse("2024-01-30")
            ),
            StrategyAnalyzeResponseDto::class.java
        )

        //then
        strategyAnalyzeResponse.statusCode shouldBe HttpStatus.OK
        val strategyParametersAnalyzeResult = strategyAnalyzeResponse.body?.analyzeResults?.first()
        strategyParametersAnalyzeResult.shouldNotBeNull()
        val strategyAnalyzeResult = strategyParametersAnalyzeResult.analyzeResult
        strategyAnalyzeResult.shouldNotBeNull()
        strategyAnalyzeResult.strategyAnalyzeResultByMonth shouldHaveSize 1
        strategyAnalyzeResult.strategyAnalyzeResultByMonth.keys.first() shouldBe YearMonth.parse("2024-01")
        strategyAnalyzeResult.grossValue shouldBe BigDecimal("2.000000000")
        strategyAnalyzeResult.netValue shouldBe BigDecimal("1.8368000000000")
        strategyAnalyzeResult.profitTradesCount shouldBe 1
        strategyAnalyzeResult.losingTradesCount shouldBe 1
        strategyAnalyzeResult.profitLossTradesRatio shouldBe BigDecimal("1.00000")
        val monthlyStrategyAnalyzeResult =
            strategyAnalyzeResult.strategyAnalyzeResultByMonth[YearMonth.parse("2024-01")]
        monthlyStrategyAnalyzeResult.shouldNotBeNull()
        monthlyStrategyAnalyzeResult.strategyAnalyzeResultByDay shouldHaveSize 1
        monthlyStrategyAnalyzeResult.netProfit shouldBe BigDecimal("8.9156000000000")
        monthlyStrategyAnalyzeResult.grossProfit shouldBe BigDecimal("9.000000000")
        monthlyStrategyAnalyzeResult.netLoss shouldBe BigDecimal("-7.0788000000000")
        monthlyStrategyAnalyzeResult.grossLoss shouldBe BigDecimal("-7.000000000")
        monthlyStrategyAnalyzeResult.netValue shouldBe BigDecimal("1.8368000000000")
        monthlyStrategyAnalyzeResult.grossValue shouldBe BigDecimal("2.000000000")
        monthlyStrategyAnalyzeResult.profitTradesCount shouldBe 1
        monthlyStrategyAnalyzeResult.losingTradesCount shouldBe 1
        monthlyStrategyAnalyzeResult.tradesCount shouldBe 3
        with(monthlyStrategyAnalyzeResult.strategyAnalyzeResultByDay.values.first()) {
            tradesCount shouldBe 3
            netProfit shouldBe BigDecimal("8.9156000000000")
            grossProfit shouldBe BigDecimal("9.000000000")
            netLoss shouldBe BigDecimal("-7.0788000000000")
            grossLoss shouldBe BigDecimal("-7.000000000")
            losingTradesCount shouldBe 1
            profitTradesCount shouldBe 1
            trades shouldHaveSize 3
            netValue shouldBe BigDecimal("1.8368000000000")
            grossValue shouldBe BigDecimal("2.000000000")
            with(trades[0]) {
                netValue shouldBe BigDecimal("8.9156000000000")
                grossValue shouldBe BigDecimal("9.000000000")
                netProfit shouldBe BigDecimal("8.9156000000000")
                grossProfit shouldBe BigDecimal("9.000000000")
                netLoss shouldBe BigDecimal.ZERO
                grossLoss shouldBe BigDecimal.ZERO
                entry.date shouldBe LocalDateTime.parse("2024-01-30T10:18")
                entry.direction shouldBe TradeDirection.BUY
                entry.grossPrice shouldBe BigDecimal("101.000000000")
                entry.netPrice shouldBe BigDecimal("101.0404000000000")
                exit?.date shouldBe LocalDateTime.parse("2024-01-30T10:20")
                exit?.direction shouldBe TradeDirection.SELL
                exit?.grossPrice shouldBe BigDecimal("110.000000000")
                exit?.netPrice shouldBe BigDecimal("109.9560000000000")
            }
            with(trades[1]) {
                netValue shouldBe BigDecimal("-7.0788000000000")
                grossValue shouldBe BigDecimal("-7.000000000")
                netProfit shouldBe BigDecimal.ZERO
                grossProfit shouldBe BigDecimal.ZERO
                netLoss shouldBe BigDecimal("-7.0788000000000")
                grossLoss shouldBe BigDecimal("-7.000000000")
                entry.date shouldBe LocalDateTime.parse("2024-01-30T10:24")
                entry.direction shouldBe TradeDirection.BUY
                entry.grossPrice shouldBe BigDecimal("102.000000000")
                entry.netPrice shouldBe BigDecimal("102.0408000000000")
                exit?.date shouldBe LocalDateTime.parse("2024-01-30T10:26")
                exit?.direction shouldBe TradeDirection.SELL
                exit?.grossPrice shouldBe BigDecimal("95.000000000")
                exit?.netPrice shouldBe BigDecimal("94.9620000000000")
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
                entry.netPrice shouldBe BigDecimal("102.0408000000000")
                exit.shouldBeNull()
            }
        }
    }

})