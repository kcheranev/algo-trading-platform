package ru.kcheranev.trading.core.strategy.factory

import org.springframework.stereotype.Component
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.RSIIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.indicators.helpers.DateTimeIndicator
import org.ta4j.core.rules.CrossedDownIndicatorRule
import org.ta4j.core.rules.CrossedUpIndicatorRule
import ru.kcheranev.trading.core.StrategyParamValidationException
import ru.kcheranev.trading.core.config.TradingProperties
import ru.kcheranev.trading.core.strategy.factory.RsiStrategyParameter.LENGTH
import ru.kcheranev.trading.core.strategy.factory.RsiStrategyParameter.OVER_BOUGHT
import ru.kcheranev.trading.core.strategy.factory.RsiStrategyParameter.OVER_SOLD
import ru.kcheranev.trading.core.strategy.rule.EndTradingTimeRule
import ru.kcheranev.trading.domain.model.CustomizedBarSeries
import ru.kcheranev.trading.domain.model.StrategyParameter
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.TradeStrategy

@Component
class RsiLongStrategyFactory(
    tradingProperties: TradingProperties
) : LongStrategyFactory() {

    private val endTradingTime = tradingProperties.endTradingTime

    override fun initStrategy(params: StrategyParameters, series: CustomizedBarSeries): TradeStrategy {
        val overSold = params.getAsInt(OVER_SOLD)
        val overBought = params.getAsInt(OVER_BOUGHT)
        val length = params.getAsInt(LENGTH)

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
        return buildTradeStrategy(series, BaseStrategy(entryRule, exitRule, length))
    }

    override fun strategyName() = "RSI"

    override fun strategyParameterNames() = RsiStrategyParameter.values().map { it.alias() }

}

private enum class RsiStrategyParameter(private val alias: String) : StrategyParameter {

    OVER_SOLD("overSold"),
    OVER_BOUGHT("overBought"),
    LENGTH("length");

    override fun alias() = alias

}