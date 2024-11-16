package ru.kcheranev.trading.core.strategy.factory

import org.springframework.stereotype.Component
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.RSIIndicator
import org.ta4j.core.indicators.SMAIndicator
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator
import org.ta4j.core.rules.StopGainRule
import org.ta4j.core.rules.StopLossRule
import org.ta4j.core.rules.UnderIndicatorRule
import ru.kcheranev.trading.domain.model.CustomizedBarSeries
import ru.kcheranev.trading.domain.model.StrategyParameter
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.TradeStrategy
import kotlin.math.max

@Component
class RsiBollingerStopGainLossLongStrategyFactory : LongStrategyFactory() {

    override fun initStrategy(parameters: StrategyParameters, series: CustomizedBarSeries): TradeStrategy {
        val overSold = parameters.getAsInt(RsiBollingerStopGainLossStrategyParameter.OVER_SOLD)
        val rsiLength = parameters.getAsInt(RsiBollingerStopGainLossStrategyParameter.RSI_LENGTH)
        val bollingerLength = parameters.getAsInt(RsiBollingerStopGainLossStrategyParameter.BOLLINGER_LENGTH)
        val gainPercentage = parameters.getAsBigDecimal(RsiBollingerStopGainLossStrategyParameter.GAIN_PERCENTAGE)
        val lossPercentage = parameters.getAsBigDecimal(RsiBollingerStopGainLossStrategyParameter.LOSS_PERCENTAGE)

        val closePrice = ClosePriceIndicator(series)
        val rsi = RSIIndicator(closePrice, rsiLength)
        val bollingerMiddle = BollingerBandsMiddleIndicator(SMAIndicator(closePrice, bollingerLength))
        val bollingerLower =
            BollingerBandsLowerIndicator(bollingerMiddle, StandardDeviationIndicator(closePrice, bollingerLength))

        val entryRule = UnderIndicatorRule(rsi, overSold)
            .and(UnderIndicatorRule(closePrice, bollingerLower))

        val exitRule = StopGainRule(ClosePriceIndicator(series), gainPercentage)
            .or(StopLossRule(ClosePriceIndicator(series), lossPercentage))
        return buildTradeStrategy(series, BaseStrategy(entryRule, exitRule, max(rsiLength, bollingerLength)))
    }

    override fun strategyName() = "RSI_BOLLINGER_STOP_GAIN_LOSS"

    override fun strategyParameterNames() = RsiBollingerStopGainLossStrategyParameter.values().map { it.alias() }

}

private enum class RsiBollingerStopGainLossStrategyParameter(private val alias: String) : StrategyParameter {

    OVER_SOLD("overSold"),
    RSI_LENGTH("rsiLength"),
    BOLLINGER_LENGTH("bollingerLength"),
    GAIN_PERCENTAGE("gainPercentage"),
    LOSS_PERCENTAGE("lossPercentage");

    override fun alias() = alias

}