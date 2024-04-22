package ru.kcheranev.trading.core.strategy.factory

import org.springframework.stereotype.Component
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.DateTimeIndicator
import org.ta4j.core.indicators.RSIIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.CrossedDownIndicatorRule
import org.ta4j.core.rules.CrossedUpIndicatorRule
import ru.kcheranev.trading.core.StrategyParamValidationException
import ru.kcheranev.trading.core.config.TradingProperties
import ru.kcheranev.trading.core.strategy.rule.EndTradingTimeRule
import ru.kcheranev.trading.domain.model.CustomizedBarSeries
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.TradeStrategy

@Component
class RsiStrategyFactory(
    tradingProperties: TradingProperties
) : StrategyFactory {

    private val endTradingTime = tradingProperties.endTradingTime

    override fun initStrategy(params: StrategyParameters, series: CustomizedBarSeries): TradeStrategy {
        val overSold = params.getAsInt("overSold")
        val overBought = params.getAsInt("overBought")
        val length = params.getAsInt("length")

        if (overSold >= overBought) {
            throw StrategyParamValidationException("overSold must be greater than overBought")
        }

        val closePrice = ClosePriceIndicator(series)
        val rsi = RSIIndicator(closePrice, length)

        val entryRule = CrossedUpIndicatorRule(rsi, overSold)
        val exitRule =
            CrossedDownIndicatorRule(rsi, overBought)
                .or(
                    EndTradingTimeRule(
                        endTradingTime,
                        series.candleInterval,
                        DateTimeIndicator(series) { bar -> bar.endTime }
                    )
                )
        return TradeStrategy(series, BaseStrategy(entryRule, exitRule, length))
    }

    override fun strategyType() = "RSI"

}