package com.github.trading.core.strategy.factory

import com.github.trading.core.strategy.factory.RsiBollingerLongStrategyParameter.BOLLINGER_LENGTH
import com.github.trading.core.strategy.factory.RsiBollingerLongStrategyParameter.OVER_BOUGHT
import com.github.trading.core.strategy.factory.RsiBollingerLongStrategyParameter.OVER_SOLD
import com.github.trading.core.strategy.factory.RsiBollingerLongStrategyParameter.RSI_LENGTH
import com.github.trading.core.util.Validator.Companion.validateOrThrow
import com.github.trading.domain.model.CustomizedBarSeries
import com.github.trading.domain.model.StrategyParameter
import com.github.trading.domain.model.StrategyParameters
import com.github.trading.domain.model.TradeStrategy
import org.springframework.stereotype.Component
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.RSIIndicator
import org.ta4j.core.indicators.SMAIndicator
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator
import org.ta4j.core.rules.CrossedDownIndicatorRule
import org.ta4j.core.rules.UnderIndicatorRule
import kotlin.math.max

@Component
class RsiBollingerLongStrategyFactory : LongStrategyFactory() {

    override fun initStrategy(parameters: StrategyParameters, series: CustomizedBarSeries): TradeStrategy {
        val overSold = parameters.getAsIntOrThrow(OVER_SOLD)
        val overBought = parameters.getAsIntOrThrow(OVER_BOUGHT)
        val rsiLength = parameters.getAsIntOrThrow(RSI_LENGTH)
        val bollingerLength = parameters.getAsIntOrThrow(BOLLINGER_LENGTH)

        validateOrThrow {
            if (overSold >= overBought) addError("overSold must be greater than overBought")
        }

        val closePrice = ClosePriceIndicator(series)
        val rsi = RSIIndicator(closePrice, rsiLength)
        val bollingerMiddle = BollingerBandsMiddleIndicator(SMAIndicator(closePrice, bollingerLength))
        val bollingerUpper =
            BollingerBandsUpperIndicator(bollingerMiddle, StandardDeviationIndicator(closePrice, bollingerLength))
        val bollingerLower =
            BollingerBandsLowerIndicator(bollingerMiddle, StandardDeviationIndicator(closePrice, bollingerLength))

        val entryRule = UnderIndicatorRule(rsi, overSold)
            .and(UnderIndicatorRule(closePrice, bollingerLower))

        val exitRule =
            CrossedDownIndicatorRule(rsi, overBought)
                .or(CrossedDownIndicatorRule(closePrice, bollingerUpper))
        return buildTradeStrategy(series, BaseStrategy(entryRule, exitRule, max(rsiLength, bollingerLength)))
    }

    override val strategyName = "RSI_BOLLINGER"

    override fun strategyParameterNames() = RsiBollingerLongStrategyParameter.entries.map { it.alias() }

}

private enum class RsiBollingerLongStrategyParameter(private val alias: String) : StrategyParameter {

    OVER_SOLD("overSold"),
    OVER_BOUGHT("overBought"),
    RSI_LENGTH("rsiLength"),
    BOLLINGER_LENGTH("bollingerLength");

    override fun alias() = alias

}