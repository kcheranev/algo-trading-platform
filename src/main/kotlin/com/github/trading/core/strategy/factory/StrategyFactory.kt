package com.github.trading.core.strategy.factory

import com.github.trading.domain.model.StrategyParameters
import com.github.trading.domain.model.TradeStrategy
import org.ta4j.core.BarSeries
import org.ta4j.core.Strategy

interface StrategyFactory {

    fun initStrategy(parameters: StrategyParameters, series: BarSeries): TradeStrategy

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

    protected abstract val strategyName: String

    override fun strategyType() = "${strategyName}_LONG"

}

abstract class ShortStrategyFactory : StrategyFactory {

    protected fun buildTradeStrategy(series: BarSeries, strategy: Strategy) =
        TradeStrategy(
            series = series,
            margin = true,
            strategy = strategy
        )

    protected abstract val strategyName: String

    override fun strategyType() = "${strategyName}_SHORT"

}