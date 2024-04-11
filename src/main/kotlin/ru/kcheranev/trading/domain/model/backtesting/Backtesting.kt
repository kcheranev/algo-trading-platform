package ru.kcheranev.trading.domain.model.backtesting

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseBarSeriesBuilder
import ru.kcheranev.trading.core.strategy.StrategyFactory
import ru.kcheranev.trading.domain.DomainException
import ru.kcheranev.trading.domain.mapper.domainModelMapper
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyParameters
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

private const val BEST_STRATEGIES_RESULT_LIMIT = 15

class Backtesting(
    val ticker: String,
    val candleInterval: CandleInterval,
    val commission: BigDecimal,
    candlesByPeriod: Map<LocalDate, List<Candle>>
) {

    private val series: Map<LocalDate, BarSeries> =
        candlesByPeriod.mapValues { dayCandles ->
            BaseBarSeriesBuilder()
                .withName("Trade session: ticker=$ticker, candleInterval=$candleInterval")
                .withBars(dayCandles.value.map { domainModelMapper.map(it) })
                .build()
        }

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
        adjustVariantCount: Int
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
        }.filterNotNull()
            .sortedByDescending { it.result.totalGrossProfit }
            .take(BEST_STRATEGIES_RESULT_LIMIT)
    }

    private fun buildAdjustedParams(
        paramValue: Number,
        adjustFactor: BigDecimal,
        adjustVariantCount: Int
    ): List<Number> =
        when (paramValue) {
            is Int -> {
                val minParamValue = BigDecimal(paramValue).divide(adjustFactor, RoundingMode.HALF_UP).toInt()
                val maxParamValue = BigDecimal(paramValue).multiply(adjustFactor).toInt()
                val paramValueStep = (maxParamValue - minParamValue) / (adjustVariantCount - 1)
                val paramVariants = mutableListOf(paramValue)
                for (paramVariant in minParamValue..maxParamValue step paramValueStep) {
                    paramVariants.add(paramVariant)
                }
                paramVariants.distinct()
            }

            is BigDecimal -> {
                val minParamValue = paramValue.divide(adjustFactor, RoundingMode.HALF_UP)
                val maxParamValue = paramValue.multiply(adjustFactor)
                val paramValueStep = (maxParamValue - minParamValue) / (BigDecimal(adjustVariantCount - 1))
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