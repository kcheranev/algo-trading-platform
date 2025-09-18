package com.github.trading.domain.model.backtesting

import com.github.trading.core.port.income.backtesting.StrategyAnalyzeResultFilter
import com.github.trading.core.port.income.backtesting.StrategyParametersMutation
import com.github.trading.core.strategy.factory.StrategyFactory
import com.github.trading.domain.exception.BusinessException
import com.github.trading.domain.mapper.domainModelMapper
import com.github.trading.domain.model.Candle
import com.github.trading.domain.model.CustomizedBarSeries
import com.github.trading.domain.model.StrategyParameters
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.ta4j.core.BaseBarSeriesBuilder
import java.math.BigDecimal
import java.math.RoundingMode

const val BACKTESTING_RESULT_SCALE = 5

private const val DEFAULT_BACKTESTING_RESULTS_LIMIT = 15

class Backtesting(
    val name: String,
    val commission: BigDecimal,
    candles: List<Candle>
) {

    private val series: CustomizedBarSeries =
        CustomizedBarSeries(
            BaseBarSeriesBuilder()
                .withName("Trade session: $name")
                .withBars(candles.map(domainModelMapper::map))
                .build()
        )

    private val daysCount =
        candles.map { it.endDateTime.toLocalDate() }
            .distinct()
            .count()

    fun analyzeStrategy(
        strategyFactory: StrategyFactory,
        parameters: StrategyParameters
    ) = strategyFactory.initStrategy(parameters, series)
        .analyze(commission)

    fun analyzeStrategyParameters(
        strategyFactory: StrategyFactory,
        parameters: StrategyParameters,
        mutableParameters: StrategyParameters,
        parametersMutation: StrategyParametersMutation,
        resultFilter: StrategyAnalyzeResultFilter?,
        profitTypeSort: ProfitTypeSort?
    ): List<StrategyParametersAnalyzeResult> {
        val mutableParametersVariants =
            mutableParameters.mapValues { (_, paramValue) ->
                buildParameterVariants(paramValue, parametersMutation.divisionFactor, parametersMutation.variantsCount)
            }
        val mutableParametersCartesianProduct = cartesianProduct(mutableParametersVariants)
        return runBlocking {
            mutableParametersCartesianProduct.map { paramVariant ->
                async { analyzeParametersVariant(strategyFactory, paramVariant + parameters) }
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

    private fun buildParameterVariants(
        paramValue: Number,
        divisionFactor: BigDecimal,
        variantsCount: Int
    ): List<Number> =
        when (paramValue) {
            is Int -> {
                val minParamValue =
                    BigDecimal(paramValue)
                        .divide(divisionFactor, BACKTESTING_RESULT_SCALE, RoundingMode.HALF_UP)
                        .toInt()
                val maxParamValue = BigDecimal(paramValue).multiply(divisionFactor).toInt()
                val paramValueStep =
                    ((maxParamValue - minParamValue) / (variantsCount - 1))
                        .let { if (it == 0) 1 else it }
                val paramVariants = mutableListOf(paramValue)
                for (paramVariant in minParamValue..maxParamValue step paramValueStep) {
                    paramVariants.add(paramVariant)
                }
                paramVariants.distinct()
            }

            is BigDecimal -> {
                val minParamValue = paramValue.divide(divisionFactor, BACKTESTING_RESULT_SCALE, RoundingMode.HALF_UP)
                val maxParamValue = paramValue.multiply(divisionFactor)
                val paramValueStep =
                    (maxParamValue - minParamValue)
                        .divide(BigDecimal(variantsCount - 1), BACKTESTING_RESULT_SCALE, RoundingMode.HALF_UP)
                val paramVariants = mutableListOf(paramValue)
                var paramVariant = minParamValue
                while (paramVariant <= maxParamValue) {
                    paramVariants.add(paramVariant)
                    paramVariant += paramValueStep
                }
                paramVariants.distinct()
            }

            else -> throw BusinessException("Unexpected parameter $paramValue value type")
        }

    private fun cartesianProduct(paramVariants: Map<String, List<Number>>): List<Map<String, Number>> =
        paramVariants.entries
            .fold(listOf(mapOf())) { accumulator, currentParameters ->
                accumulator.flatMap { map ->
                    currentParameters.value
                        .map { mutableMapOf(currentParameters.key to it) + map }
                }
            }

    private fun analyzeParametersVariant(
        strategyFactory: StrategyFactory,
        parameters: Map<String, Number>
    ): StrategyParametersAnalyzeResult? =
        try {
            StrategyParametersAnalyzeResult(analyzeStrategy(strategyFactory, StrategyParameters(parameters)), parameters)
        } catch (_: Exception) {
            null
        }

}