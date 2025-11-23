package com.github.trading.domain.model.backtesting

import com.github.trading.core.port.income.backtesting.StrategyAnalyzeResultFilter
import com.github.trading.core.port.income.backtesting.StrategyParametersMutation
import com.github.trading.core.strategy.factory.StrategyFactory
import com.github.trading.domain.mapper.domainModelMapper
import com.github.trading.domain.model.Candle
import com.github.trading.domain.model.StrategyParameters
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseBarSeriesBuilder
import java.math.BigDecimal

const val BACKTESTING_RESULT_SCALE = 5

private const val DEFAULT_BACKTESTING_RESULTS_LIMIT = 15

class Backtesting(
    val name: String,
    val commission: BigDecimal,
    candles: List<Candle>
) {

    private val series: BarSeries =
        BaseBarSeriesBuilder()
            .withName("Trade session: $name")
            .build()

    private val daysCount =
        candles.map { it.endDateTime.toLocalDate() }
            .distinct()
            .count()

    init {
        candles.forEach { candle ->
            series.addBar(domainModelMapper.map(candle, series.barBuilder()))
        }
    }

    fun analyzeStrategy(
        strategyFactory: StrategyFactory,
        parameters: Map<String, Number>,
        mutableParameters: Map<String, MutableParameter>,
        parametersMutation: StrategyParametersMutation,
        resultFilter: StrategyAnalyzeResultFilter?,
        profitTypeSort: ProfitTypeSort?
    ): List<StrategyParametersAnalyzeResult> {
        val mutableParametersVariants =
            mutableParameters.mapValues { (_, mutableParameter) ->
                mutableParameter.getParameterVariants(parametersMutation.divisionFactor, parametersMutation.variantsCount)
            }
        val mutableParametersCartesianProduct = cartesianProduct(mutableParametersVariants)
        return runBlocking {
            mutableParametersCartesianProduct.map { paramVariant ->
                async { analyzeStrategyVariant(strategyFactory, paramVariant + parameters) }
            }.awaitAll()
        }.asSequence()
            .filterNotNull()
            .filter { parametersAnalyzeResult ->
                resultFilter?.minProfitLossTradesRatio == null ||
                        parametersAnalyzeResult.analyzeResult.profitLossTradesRatio >= resultFilter.minProfitLossTradesRatio
            }
            .filter { parametersAnalyzeResult ->
                resultFilter?.tradesByDayCountFactor == null ||
                        parametersAnalyzeResult.analyzeResult.tradesCount >=
                        BigDecimal(daysCount)
                            .multiply(resultFilter.tradesByDayCountFactor)
                            .toInt()
            }
            .sortedByDescending {
                when (profitTypeSort) {
                    ProfitTypeSort.NET -> it.analyzeResult.netValue
                    ProfitTypeSort.GROSS -> it.analyzeResult.grossValue
                    null -> it.analyzeResult.netValue
                }
            }
            .take(resultFilter?.resultsLimit ?: DEFAULT_BACKTESTING_RESULTS_LIMIT)
            .toList()
    }

    private fun cartesianProduct(paramVariants: Map<String, List<Number>>): List<Map<String, Number>> =
        paramVariants.entries
            .fold(listOf(mapOf())) { accumulator, currentParameters ->
                accumulator.flatMap { map ->
                    currentParameters.value
                        .map { mutableMapOf(currentParameters.key to it) + map }
                }
            }

    private fun analyzeStrategyVariant(strategyFactory: StrategyFactory, parameters: Map<String, Number>): StrategyParametersAnalyzeResult? =
        try {
            strategyFactory.initStrategy(StrategyParameters(parameters), series)
                .analyze(commission)
                .let { result -> StrategyParametersAnalyzeResult(result, parameters) }
        } catch (_: Exception) {
            null
        }

}