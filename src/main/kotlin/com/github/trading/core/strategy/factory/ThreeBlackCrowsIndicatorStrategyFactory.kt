package com.github.trading.core.strategy.factory

import com.github.trading.core.strategy.rule.StopGainRule
import com.github.trading.core.strategy.rule.StopLossRule
import com.github.trading.domain.model.StrategyParameter
import com.github.trading.domain.model.StrategyParameters
import com.github.trading.domain.model.TradeStrategy
import org.springframework.stereotype.Component
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.candles.ThreeBlackCrowsIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.BooleanIndicatorRule

@Component
class ThreeBlackCrowsIndicatorStrategyFactory : ShortStrategyFactory() {

    override fun initStrategy(parameters: StrategyParameters, series: BarSeries): TradeStrategy {
        val barCount = parameters.getAsIntOrThrow(ThreeBlackCrowsStrategyLongParameter.BAR_COUNT)
        val factor = parameters.getAsBigDecimalOrThrow(ThreeBlackCrowsStrategyLongParameter.FACTOR)
        val gainPercentage = parameters.getAsBigDecimalOrThrow(ThreeBlackCrowsStrategyLongParameter.GAIN_PERCENTAGE)
        val lossPercentage = parameters.getAsBigDecimalOrThrow(ThreeBlackCrowsStrategyLongParameter.LOSS_PERCENTAGE)

        val bullishEngulfingIndicator = ThreeBlackCrowsIndicator(series, barCount, factor.toDouble())

        val entryRule = BooleanIndicatorRule(bullishEngulfingIndicator)
        val exitRule = StopGainRule(ClosePriceIndicator(series), gainPercentage)
            .or(StopLossRule(ClosePriceIndicator(series), lossPercentage))
        return buildTradeStrategy(series, BaseStrategy(entryRule, exitRule, 1))
    }

    override val strategyName = "THREE_BLACK_CROWS"

    override fun strategyParameterNames() = ThreeBlackCrowsStrategyLongParameter.entries.map(ThreeBlackCrowsStrategyLongParameter::alias)

}

private enum class ThreeBlackCrowsStrategyLongParameter(private val alias: String) : StrategyParameter {

    BAR_COUNT("barCount"),
    FACTOR("factor"),
    GAIN_PERCENTAGE("gainPercentage"),
    LOSS_PERCENTAGE("lossPercentage");

    override fun alias() = alias

}