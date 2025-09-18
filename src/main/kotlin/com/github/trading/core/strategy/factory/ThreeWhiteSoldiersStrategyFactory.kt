package com.github.trading.core.strategy.factory

import com.github.trading.core.strategy.rule.StopGainRule
import com.github.trading.core.strategy.rule.StopLossRule
import com.github.trading.domain.model.CustomizedBarSeries
import com.github.trading.domain.model.StrategyParameter
import com.github.trading.domain.model.StrategyParameters
import com.github.trading.domain.model.TradeStrategy
import org.springframework.stereotype.Component
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.candles.ThreeWhiteSoldiersIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.num.DecimalNum
import org.ta4j.core.rules.BooleanIndicatorRule

@Component
class ThreeWhiteSoldiersStrategyFactory : LongStrategyFactory() {

    override fun initStrategy(parameters: StrategyParameters, series: CustomizedBarSeries): TradeStrategy {
        val barCount = parameters.getAsIntOrThrow(ThreeWhiteSoldiersStrategyLongParameter.BAR_COUNT)
        val factor = parameters.getAsBigDecimalOrThrow(ThreeWhiteSoldiersStrategyLongParameter.FACTOR)
        val gainPercentage = parameters.getAsBigDecimalOrThrow(ThreeWhiteSoldiersStrategyLongParameter.GAIN_PERCENTAGE)
        val lossPercentage = parameters.getAsBigDecimalOrThrow(ThreeWhiteSoldiersStrategyLongParameter.LOSS_PERCENTAGE)

        val bullishEngulfingIndicator = ThreeWhiteSoldiersIndicator(series, barCount, DecimalNum.valueOf(factor, 4))

        val entryRule = BooleanIndicatorRule(bullishEngulfingIndicator)
        val exitRule = StopGainRule(ClosePriceIndicator(series), gainPercentage)
            .or(StopLossRule(ClosePriceIndicator(series), lossPercentage))
        return buildTradeStrategy(series, BaseStrategy(entryRule, exitRule, 1))
    }

    override val strategyName = "THREE_WHITE_SOLDIERS"

    override fun strategyParameterNames() = ThreeWhiteSoldiersStrategyLongParameter.entries.map { it.alias() }

}

private enum class ThreeWhiteSoldiersStrategyLongParameter(private val alias: String) : StrategyParameter {

    BAR_COUNT("barCount"),
    FACTOR("factor"),
    GAIN_PERCENTAGE("gainPercentage"),
    LOSS_PERCENTAGE("lossPercentage");

    override fun alias() = alias

}