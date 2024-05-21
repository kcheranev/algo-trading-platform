package ru.kcheranev.trading.domain.model.backtesting

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.ta4j.core.BaseBarSeriesBuilder
import ru.kcheranev.trading.common.isWeekend
import ru.kcheranev.trading.core.strategy.factory.StrategyFactory
import ru.kcheranev.trading.domain.DomainException
import ru.kcheranev.trading.domain.mapper.domainModelMapper
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.CustomizedBarSeries
import ru.kcheranev.trading.domain.model.StrategyParameters
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

const val BACKTESTING_RESULT_SCALE = 5

private const val DEFAULT_BACKTESTING_RESULTS_LIMIT = 15

private val DEFAULT_MIN_PROFIT_LOSS_POSITIONS_RATIO = BigDecimal(1)

private val DEFAULT_TRADES_BY_DAY_COUNT_FACTOR = BigDecimal(1)

class Backtesting(
    val ticker: String,
    val candleInterval: CandleInterval,
    val commission: BigDecimal,
    candlesByPeriod: Map<LocalDate, List<Candle>>
) {

    private val series: Map<LocalDate, CustomizedBarSeries> =
        candlesByPeriod.mapValues { dayCandles ->
            CustomizedBarSeries(
                BaseBarSeriesBuilder()
                    .withName("Trade session: ticker=$ticker, candleInterval=$candleInterval")
                    .withBars(dayCandles.value.map { domainModelMapper.map(it) })
                    .build(),
                candleInterval
            )
        }

    private val daysCount = candlesByPeriod.keys.count { !it.isWeekend() }

    fun analyzeStrategy(
        strategyFactory: StrategyFactory,
        params: StrategyParameters
    ) = PeriodStrategyAnalyzeResult(
        series.mapValues {
            strategyFactory.initStrategy(params, it.value)
                .analyze(commission)
        }
    )

    fun adjustAndAnalyzeStrategy(
        strategyFactory: StrategyFactory,
        params: StrategyParameters,
        mutableParams: StrategyParameters,
        adjustFactor: BigDecimal,
        adjustVariantCount: Int,
        resultsLimit: Int?,
        minProfitLossPositionsRatio: BigDecimal?,
        tradesByDayCountFactor: BigDecimal?,
        profitTypeSort: ProfitTypeSort?
    ): List<StrategyAdjustAndAnalyzeResult> {
        val adjustedParams =
            mutableParams.mapValues { (_, paramValue) ->
                buildAdjustedParams(paramValue, adjustFactor, adjustVariantCount)
            }
        val adjustedParamVariants = cartesianProduct(adjustedParams)
        return runBlocking {
            adjustedParamVariants.map { paramVariant ->
                async {
                    analyzeAdjustedStrategy(strategyFactory, paramVariant + params)
                }
            }.awaitAll()
        }.asSequence()
            .filterNotNull()
            .filter { it.result.totalNetProfit > BigDecimal.ZERO }
            .filter {
                it.result.profitLossPositionsRatio >=
                        (minProfitLossPositionsRatio ?: DEFAULT_MIN_PROFIT_LOSS_POSITIONS_RATIO)
            }
            .filter {
                it.result.tradesCount >=
                        (BigDecimal(daysCount)
                            .multiply(tradesByDayCountFactor ?: DEFAULT_TRADES_BY_DAY_COUNT_FACTOR))
                            .toInt()
            }
            .sortedByDescending {
                when (profitTypeSort) {
                    ProfitTypeSort.NET -> it.result.totalNetProfit
                    ProfitTypeSort.GROSS -> it.result.totalGrossProfit
                    null -> it.result.totalNetProfit
                }
            }
            .take(resultsLimit ?: DEFAULT_BACKTESTING_RESULTS_LIMIT)
            .toList()
    }

    private fun buildAdjustedParams(
        paramValue: Number,
        adjustFactor: BigDecimal,
        adjustVariantCount: Int
    ): List<Number> =
        when (paramValue) {
            is Int -> {
                val minParamValue =
                    BigDecimal(paramValue)
                        .divide(adjustFactor, BACKTESTING_RESULT_SCALE, RoundingMode.HALF_UP)
                        .toInt()
                val maxParamValue = BigDecimal(paramValue).multiply(adjustFactor).toInt()
                val paramValueStep =
                    ((maxParamValue - minParamValue) / (adjustVariantCount - 1))
                        .let { if (it == 0) 1 else it }
                val paramVariants = mutableListOf(paramValue)
                for (paramVariant in minParamValue..maxParamValue step paramValueStep) {
                    paramVariants.add(paramVariant)
                }
                paramVariants.distinct()
            }

            is BigDecimal -> {
                val minParamValue = paramValue.divide(adjustFactor, BACKTESTING_RESULT_SCALE, RoundingMode.HALF_UP)
                val maxParamValue = paramValue.multiply(adjustFactor)
                val paramValueStep =
                    (maxParamValue - minParamValue)
                        .divide(BigDecimal(adjustVariantCount - 1), BACKTESTING_RESULT_SCALE, RoundingMode.HALF_UP)
                val paramVariants = mutableListOf(paramValue)
                var paramVariant = minParamValue
                while (paramValue <= maxParamValue) {
                    paramVariants.add(paramVariant)
                    paramVariant += paramValueStep
                }
                paramVariants.distinct()
            }

            else -> throw DomainException("Unexpected parameter $paramValue value type")
        }

    private fun cartesianProduct(paramVariants: Map<String, List<Number>>): List<Map<String, Number>> =
        paramVariants.entries
            .fold(listOf(mapOf())) { accumulator, currentParams ->
                accumulator.flatMap { map ->
                    currentParams.value
                        .map { mutableMapOf(currentParams.key to it) + map }
                }
            }

    private fun analyzeAdjustedStrategy(
        strategyFactory: StrategyFactory,
        params: Map<String, Number>
    ): StrategyAdjustAndAnalyzeResult? =
        try {
            StrategyAdjustAndAnalyzeResult(analyzeStrategy(strategyFactory, StrategyParameters(params)), params)
        } catch (e: Exception) {
            //ignore
            null
        }

}