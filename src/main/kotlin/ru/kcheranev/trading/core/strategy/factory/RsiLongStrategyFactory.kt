package ru.kcheranev.trading.core.strategy.factory

import org.springframework.stereotype.Component
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.RSIIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.CrossedDownIndicatorRule
import org.ta4j.core.rules.CrossedUpIndicatorRule
import ru.kcheranev.trading.core.exception.StrategyParamValidationException
import ru.kcheranev.trading.core.strategy.factory.RsiStrategyLongParameter.LENGTH
import ru.kcheranev.trading.core.strategy.factory.RsiStrategyLongParameter.OVER_BOUGHT
import ru.kcheranev.trading.core.strategy.factory.RsiStrategyLongParameter.OVER_SOLD
import ru.kcheranev.trading.domain.model.CustomizedBarSeries
import ru.kcheranev.trading.domain.model.StrategyParameter
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.TradeStrategy

@Component
class RsiLongStrategyFactory : LongStrategyFactory() {

    override fun initStrategy(parameters: StrategyParameters, series: CustomizedBarSeries): TradeStrategy {
        val overSold = parameters.getAsInt(OVER_SOLD)
        val overBought = parameters.getAsInt(OVER_BOUGHT)
        val length = parameters.getAsInt(LENGTH)

        if (overSold >= overBought) {
            throw StrategyParamValidationException("overSold must be greater than overBought")
        }

        val closePrice = ClosePriceIndicator(series)
        val rsi = RSIIndicator(closePrice, length)

        val entryRule = CrossedUpIndicatorRule(rsi, overSold)
        val exitRule = CrossedDownIndicatorRule(rsi, overBought)
        return buildTradeStrategy(series, BaseStrategy(entryRule, exitRule, length))
    }

    override val strategyName = "RSI"

    override fun strategyParameterNames() = RsiStrategyLongParameter.entries.map { it.alias() }

}

private enum class RsiStrategyLongParameter(private val alias: String) : StrategyParameter {

    OVER_SOLD("overSold"),
    OVER_BOUGHT("overBought"),
    LENGTH("length");

    override fun alias() = alias

}