package ru.kcheranev.trading.core.strategy.factory

import org.springframework.stereotype.Component
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.candles.BullishEngulfingIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.BooleanIndicatorRule
import org.ta4j.core.rules.StopGainRule
import org.ta4j.core.rules.StopLossRule
import ru.kcheranev.trading.domain.model.CustomizedBarSeries
import ru.kcheranev.trading.domain.model.StrategyParameter
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.TradeStrategy

@Component
class BullishEngulfingStrategyFactory : LongStrategyFactory() {

    override fun initStrategy(parameters: StrategyParameters, series: CustomizedBarSeries): TradeStrategy {
        val gainPercentage = parameters.getAsBigDecimal(BullishEngulfingStrategyLongParameter.GAIN_PERCENTAGE)
        val lossPercentage = parameters.getAsBigDecimal(BullishEngulfingStrategyLongParameter.LOSS_PERCENTAGE)

        val bullishEngulfingIndicator = BullishEngulfingIndicator(series)

        val entryRule = BooleanIndicatorRule(bullishEngulfingIndicator)
        val exitRule = StopGainRule(ClosePriceIndicator(series), gainPercentage)
            .or(StopLossRule(ClosePriceIndicator(series), lossPercentage))
        return buildTradeStrategy(series, BaseStrategy(entryRule, exitRule, 1))
    }

    override fun strategyName() = "BULLISH_ENGULFING"

    override fun strategyParameterNames() = BullishEngulfingStrategyLongParameter.values().map { it.alias() }

}

private enum class BullishEngulfingStrategyLongParameter(private val alias: String) : StrategyParameter {

    GAIN_PERCENTAGE("gainPercentage"),
    LOSS_PERCENTAGE("lossPercentage");

    override fun alias() = alias

}