package ru.kcheranev.trading.core.strategy

import org.springframework.stereotype.Component
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.EMAIndicator
import org.ta4j.core.indicators.MACDIndicator
import org.ta4j.core.indicators.StochasticOscillatorKIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.CrossedDownIndicatorRule
import org.ta4j.core.rules.CrossedUpIndicatorRule
import org.ta4j.core.rules.OverIndicatorRule
import org.ta4j.core.rules.UnderIndicatorRule
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.StrategyType
import ru.kcheranev.trading.domain.model.TradeStrategy

@Component
class MovingMomentumStrategyFactory : StrategyFactory {

    override fun initStrategy(
        params: StrategyParameters,
        series: BarSeries
    ): TradeStrategy {
        val shortEmaBarCount = params.getAsInt("shortEmaBarCount")
        val longEmaBarCount = params.getAsInt("longEmaBarCount")
        val shortMacdBarCount = params.getAsInt("shortMacdBarCount")
        val longMacdBarCount = params.getAsInt("longMacdBarCount")
        val emaMacdBarCount = params.getAsInt("emaMacdBarCount")
        val stochasticOscillatorKBarCount = params.getAsInt("stochasticOscillatorKBarCount")

        val closePrice = ClosePriceIndicator(series)
        val shortEma = EMAIndicator(closePrice, shortEmaBarCount)
        val longEma = EMAIndicator(closePrice, longEmaBarCount)

        val stochasticOscillK = StochasticOscillatorKIndicator(series, stochasticOscillatorKBarCount)

        val macd = MACDIndicator(closePrice, shortMacdBarCount, longMacdBarCount)
        val emaMacd = EMAIndicator(macd, emaMacdBarCount)

        val entryRule = OverIndicatorRule(shortEma, longEma)
            .and(CrossedDownIndicatorRule(stochasticOscillK, 20))
            .and(OverIndicatorRule(macd, emaMacd))

        val exitRule = UnderIndicatorRule(shortEma, longEma)
            .and(CrossedUpIndicatorRule(stochasticOscillK, 80))
            .and(UnderIndicatorRule(macd, emaMacd))

        return TradeStrategy(series, BaseStrategy(entryRule, exitRule))
    }

    override fun strategyType(): String {
        return StrategyType.MOVING_MOMENTUM.name
    }

}