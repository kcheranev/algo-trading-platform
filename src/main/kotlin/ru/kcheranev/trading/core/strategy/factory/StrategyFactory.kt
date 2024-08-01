package ru.kcheranev.trading.core.strategy.factory

import org.ta4j.core.BarSeries
import org.ta4j.core.Strategy
import ru.kcheranev.trading.domain.model.CustomizedBarSeries
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.TradeStrategy

interface StrategyFactory {

    fun initStrategy(
        params: StrategyParameters,
        series: CustomizedBarSeries
    ): TradeStrategy

    fun strategyType(): String

    fun strategyParameterNames(): List<String>

}

abstract class LongStrategyFactory : StrategyFactory {

    protected fun buildTradeStrategy(series: BarSeries, strategy: Strategy) =
        TradeStrategy(
            series = series,
            margin = false,
            strategy = strategy
        )

    protected abstract fun strategyName(): String

    override fun strategyType() = strategyName() + "_LONG"

}

abstract class ShortStrategyFactory : StrategyFactory {

    protected fun buildTradeStrategy(series: BarSeries, strategy: Strategy) =
        TradeStrategy(
            series = series,
            margin = true,
            strategy = strategy
        )

    protected abstract fun strategyName(): String

    override fun strategyType() = strategyName() + "_SHORT"

}