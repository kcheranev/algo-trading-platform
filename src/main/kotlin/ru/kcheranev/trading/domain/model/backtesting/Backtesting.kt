package ru.kcheranev.trading.domain.model.backtesting

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseBarSeriesBuilder
import ru.kcheranev.trading.core.strategy.StrategyFactory
import ru.kcheranev.trading.domain.mapper.domainModelMapper
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyParameters
import java.math.BigDecimal
import java.math.RoundingMode

private const val BEST_STRATEGIES_RESULT_LIMIT = 15

class Backtesting(
    val ticker: String,
    val candleInterval: CandleInterval,
    candles: List<Candle>
) {

    private val series: BarSeries =
        BaseBarSeriesBuilder()
            .withName("Trade session: ticker=$ticker, candleInterval=$candleInterval")
            .withBars(candles.map { domainModelMapper.map(it) })
            .build()

    fun analyzeStrategy(
        strategyFactory: StrategyFactory,
        params: StrategyParameters
    ) = strategyFactory.initStrategy(params, series).analyze()

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
        paramValue: Int,
        adjustFactor: BigDecimal,
        adjustVariantCount: Int
    ): List<Int> {
        val minParamValue = BigDecimal(paramValue).divide(adjustFactor, RoundingMode.HALF_UP).toInt()
        val maxParamValue = BigDecimal(paramValue).multiply(adjustFactor).toInt()
        val paramValueStep = (maxParamValue - minParamValue) / (adjustVariantCount - 1)
        val paramVariants = mutableListOf(paramValue)
        for (paramVariant in minParamValue..maxParamValue step paramValueStep) {
            paramVariants.add(paramVariant)
        }
        return paramVariants.distinct()
    }

    private fun cartesianProduct(paramVariants: Map<String, List<Int>>): List<Map<String, Int>> =
        paramVariants.entries
            .fold(listOf(mapOf())) { accumulator, currentParams ->
                accumulator.flatMap { map ->
                    currentParams.value
                        .map { mutableMapOf(currentParams.key to it) + map }
                }
            }

    private fun analyzeAdjustedStrategy(
        strategyFactory: StrategyFactory,
        params: Map<String, Int>
    ): StrategyAdjustAndAnalyzeResult? =
        try {
            StrategyAdjustAndAnalyzeResult(analyzeStrategy(strategyFactory, StrategyParameters(params)), params)
        } catch (e: Exception) {
            //ignore
            null
        }

}