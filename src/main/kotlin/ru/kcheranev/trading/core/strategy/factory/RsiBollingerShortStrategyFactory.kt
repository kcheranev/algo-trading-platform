package ru.kcheranev.trading.core.strategy.factory

import org.springframework.stereotype.Component
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.RSIIndicator
import org.ta4j.core.indicators.SMAIndicator
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator
import org.ta4j.core.rules.CrossedUpIndicatorRule
import org.ta4j.core.rules.OverIndicatorRule
import ru.kcheranev.trading.core.exception.StrategyParamValidationException
import ru.kcheranev.trading.core.strategy.factory.RsiBollingerShortStrategyParameter.BOLLINGER_LENGTH
import ru.kcheranev.trading.core.strategy.factory.RsiBollingerShortStrategyParameter.OVER_BOUGHT
import ru.kcheranev.trading.core.strategy.factory.RsiBollingerShortStrategyParameter.OVER_SOLD
import ru.kcheranev.trading.core.strategy.factory.RsiBollingerShortStrategyParameter.RSI_LENGTH
import ru.kcheranev.trading.domain.model.CustomizedBarSeries
import ru.kcheranev.trading.domain.model.StrategyParameter
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.TradeStrategy
import kotlin.math.max

@Component
class RsiBollingerShortStrategyFactory : ShortStrategyFactory() {

    override fun initStrategy(parameters: StrategyParameters, series: CustomizedBarSeries): TradeStrategy {
        val overSold = parameters.getAsInt(OVER_SOLD)
        val overBought = parameters.getAsInt(OVER_BOUGHT)
        val rsiLength = parameters.getAsInt(RSI_LENGTH)
        val bollingerLength = parameters.getAsInt(BOLLINGER_LENGTH)

        if (overSold >= overBought) {
            throw StrategyParamValidationException("overSold must be greater than overBought")
        }

        val closePrice = ClosePriceIndicator(series)
        val rsi = RSIIndicator(closePrice, rsiLength)
        val bollingerMiddle = BollingerBandsMiddleIndicator(SMAIndicator(closePrice, bollingerLength))
        val bollingerUpper =
            BollingerBandsUpperIndicator(bollingerMiddle, StandardDeviationIndicator(closePrice, bollingerLength))
        val bollingerLower =
            BollingerBandsLowerIndicator(bollingerMiddle, StandardDeviationIndicator(closePrice, bollingerLength))

        val entryRule = OverIndicatorRule(rsi, overBought)
            .and(OverIndicatorRule(closePrice, bollingerUpper))

        val exitRule =
            CrossedUpIndicatorRule(rsi, overSold)
                .or(CrossedUpIndicatorRule(closePrice, bollingerLower))
        return buildTradeStrategy(series, BaseStrategy(entryRule, exitRule, max(rsiLength, bollingerLength)))
    }

    override fun strategyName() = "RSI_BOLLINGER"

    override fun strategyParameterNames() = RsiBollingerShortStrategyParameter.entries.map { it.alias() }

}

private enum class RsiBollingerShortStrategyParameter(private val alias: String) : StrategyParameter {

    OVER_SOLD("overSold"),
    OVER_BOUGHT("overBought"),
    RSI_LENGTH("rsiLength"),
    BOLLINGER_LENGTH("bollingerLength");

    override fun alias() = alias

}