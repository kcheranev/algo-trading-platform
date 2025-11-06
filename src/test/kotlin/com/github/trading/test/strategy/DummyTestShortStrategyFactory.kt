package com.github.trading.test.strategy

import com.github.trading.core.strategy.factory.ShortStrategyFactory
import com.github.trading.domain.model.StrategyParameters
import com.github.trading.domain.model.TradeStrategy
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.CrossedDownIndicatorRule
import org.ta4j.core.rules.CrossedUpIndicatorRule
import java.math.BigDecimal

class DummyTestShortStrategyFactory : ShortStrategyFactory() {

    override fun initStrategy(parameters: StrategyParameters, series: BarSeries): TradeStrategy {
        val closePrice = ClosePriceIndicator(series)
        val entryRule = CrossedDownIndicatorRule(closePrice, BigDecimal(105))
        val exitRule =
            CrossedDownIndicatorRule(closePrice, BigDecimal(100))
                .or(CrossedUpIndicatorRule(closePrice, BigDecimal(107)))
        return buildTradeStrategy(series, BaseStrategy(entryRule, exitRule, 4))
    }

    override val strategyName = "DUMMY"

    override fun strategyParameterNames() = emptyList<String>()

}