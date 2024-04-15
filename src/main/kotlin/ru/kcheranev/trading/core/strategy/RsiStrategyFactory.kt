package ru.kcheranev.trading.core.strategy

import org.springframework.stereotype.Component
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.RSIIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.CrossedDownIndicatorRule
import org.ta4j.core.rules.CrossedUpIndicatorRule
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.TradeStrategy

@Component
class RsiStrategyFactory : StrategyFactory {

    override fun initStrategy(params: StrategyParameters, series: BarSeries): TradeStrategy {
        val overSold = params.getAsInt("overSold")
        val overBought = params.getAsInt("overBought")
        val length = params.getAsInt("length")

        val closePrice = ClosePriceIndicator(series)
        val rsi = RSIIndicator(closePrice, length)

        val entryRule = CrossedUpIndicatorRule(rsi, overSold)
        val exitRule = CrossedDownIndicatorRule(rsi, overBought)

        return TradeStrategy(series, BaseStrategy(entryRule, exitRule))
    }

    override fun strategyType() = "RSI"

}