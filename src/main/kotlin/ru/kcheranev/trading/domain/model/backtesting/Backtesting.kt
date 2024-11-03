package ru.kcheranev.trading.domain.model.backtesting

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.ta4j.core.BaseBarSeriesBuilder
import ru.kcheranev.trading.core.strategy.factory.StrategyFactory
import ru.kcheranev.trading.domain.exception.BusinessException
import ru.kcheranev.trading.domain.mapper.domainModelMapper
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.CustomizedBarSeries
import ru.kcheranev.trading.domain.model.StrategyParameters
import java.math.BigDecimal
import java.math.RoundingMode

const val BACKTESTING_RESULT_SCALE = 5

private const val DEFAULT_BACKTESTING_RESULTS_LIMIT = 15

private val DEFAULT_MIN_PROFIT_LOSS_POSITIONS_RATIO = BigDecimal(1)

private val DEFAULT_TRADES_BY_DAY_COUNT_FACTOR = BigDecimal(1)

class Backtesting(
    val ticker: String,
    val candleInterval: CandleInterval,
    val commission: BigDecimal,
    candles: List<Candle>
) {

    private val series: CustomizedBarSeries =
        CustomizedBarSeries(
            BaseBarSeriesBuilder()
                .withName("Trade session: ticker=$ticker, candleInterval=$candleInterval")
                .withBars(candles.map { domainModelMapper.map(it) })
                .build(),
            candleInterval
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
        divisionFactor: BigDecimal,
        variantsCount: Int,
        resultsLimit: Int?,
        minProfitLossPositionsRatio: BigDecimal?,
        tradesByDayCountFactor: BigDecimal?,
        profitTypeSort: ProfitTypeSort?
    ): List<StrategyParametersAnalyzeResult> {
        val mutableParametersVariants =
            mutableParameters.mapValues { (_, paramValue) ->
                buildParameterVariants(paramValue, divisionFactor, variantsCount)
            }
        val mutableParametersCartesianProduct = cartesianProduct(mutableParametersVariants)
        return runBlocking {
            mutableParametersCartesianProduct.map { paramVariant ->
                async {
                    analyzeParametersVariant(strategyFactory, paramVariant + parameters)
                }
            }.awaitAll()
        }.asSequence()
            .filterNotNull()
            .filter {
                it.analyzeResult.profitLossPositionsRatio >=
                        (minProfitLossPositionsRatio ?: DEFAULT_MIN_PROFIT_LOSS_POSITIONS_RATIO)
            }
            .filter {
                it.analyzeResult.trades.size >=
                        BigDecimal(daysCount)
                            .multiply(tradesByDayCountFactor ?: DEFAULT_TRADES_BY_DAY_COUNT_FACTOR)
                            .toInt()
            }
            .sortedByDescending {
                when (profitTypeSort) {
                    ProfitTypeSort.NET -> it.analyzeResult.netValue
                    ProfitTypeSort.GROSS -> it.analyzeResult.grossValue
                    null -> it.analyzeResult.netValue
                }
            }
            .take(resultsLimit ?: DEFAULT_BACKTESTING_RESULTS_LIMIT)
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
            StrategyParametersAnalyzeResult(
                analyzeStrategy(strategyFactory, StrategyParameters(parameters)),
                parameters
            )
        } catch (e: Exception) {
            //ignore
            null
        }

}