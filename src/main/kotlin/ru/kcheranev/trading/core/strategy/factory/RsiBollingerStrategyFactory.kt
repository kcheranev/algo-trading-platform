package ru.kcheranev.trading.core.strategy.factory

import org.springframework.stereotype.Component
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.DateTimeIndicator
import org.ta4j.core.indicators.RSIIndicator
import org.ta4j.core.indicators.SMAIndicator
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator
import org.ta4j.core.rules.CrossedDownIndicatorRule
import org.ta4j.core.rules.UnderIndicatorRule
import ru.kcheranev.trading.core.StrategyParamValidationException
import ru.kcheranev.trading.core.config.TradingProperties
import ru.kcheranev.trading.core.strategy.rule.EndTradingTimeRule
import ru.kcheranev.trading.domain.model.CustomizedBarSeries
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.TradeStrategy
import kotlin.math.max

@Component
class RsiBollingerStrategyFactory(
    tradingProperties: TradingProperties
) : StrategyFactory {

    private val endTradingTime = tradingProperties.endTradingTime

    override fun initStrategy(params: StrategyParameters, series: CustomizedBarSeries): TradeStrategy {
        val overSold = params.getAsInt("overSold")
        val overBought = params.getAsInt("overBought")
        val rsiLength = params.getAsInt("rsiLength")
        val bollingerLength = params.getAsInt("bollingerLength")

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

        val entryRule = UnderIndicatorRule(rsi, overSold)
            .and(UnderIndicatorRule(closePrice, bollingerLower))

        val exitRule =
            CrossedDownIndicatorRule(rsi, overBought)
                .or(CrossedDownIndicatorRule(closePrice, bollingerUpper))
                .or(
                    EndTradingTimeRule(
                        endTradingTime,
                        series.candleInterval,
                        DateTimeIndicator(series) { bar -> bar.endTime }
                    )
                )
        return TradeStrategy(series, BaseStrategy(entryRule, exitRule, max(rsiLength, bollingerLength)))
    }

    override fun strategyType() = "RSI_BOLLINGER"

}