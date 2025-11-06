package com.github.trading.core.strategy.factory

import com.github.trading.core.strategy.factory.RsiStrategyLongParameter.LENGTH
import com.github.trading.core.strategy.factory.RsiStrategyLongParameter.OVER_BOUGHT
import com.github.trading.core.strategy.factory.RsiStrategyLongParameter.OVER_SOLD
import com.github.trading.core.util.Validator.Companion.validateOrThrow
import com.github.trading.domain.model.StrategyParameter
import com.github.trading.domain.model.StrategyParameters
import com.github.trading.domain.model.TradeStrategy
import org.springframework.stereotype.Component
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.RSIIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.CrossedDownIndicatorRule
import org.ta4j.core.rules.CrossedUpIndicatorRule

@Component
class RsiLongStrategyFactory : LongStrategyFactory() {

    override fun initStrategy(parameters: StrategyParameters, series: BarSeries): TradeStrategy {
        val overSold = parameters.getAsIntOrThrow(OVER_SOLD)
        val overBought = parameters.getAsIntOrThrow(OVER_BOUGHT)
        val length = parameters.getAsIntOrThrow(LENGTH)

        validateOrThrow {
            if (overSold >= overBought) addError("overSold must be greater than overBought")
        }

        val closePrice = ClosePriceIndicator(series)
        val rsi = RSIIndicator(closePrice, length)

        val entryRule = CrossedUpIndicatorRule(rsi, overSold)
        val exitRule = CrossedDownIndicatorRule(rsi, overBought)
        return buildTradeStrategy(series, BaseStrategy(entryRule, exitRule, length))
    }

    override val strategyName = "RSI"

    override fun strategyParameterNames() = RsiStrategyLongParameter.entries.map(RsiStrategyLongParameter::alias)

}

private enum class RsiStrategyLongParameter(private val alias: String) : StrategyParameter {

    OVER_SOLD("overSold"),
    OVER_BOUGHT("overBought"),
    LENGTH("length");

    override fun alias() = alias

}