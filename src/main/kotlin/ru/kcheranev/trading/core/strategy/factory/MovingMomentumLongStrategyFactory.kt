package ru.kcheranev.trading.core.strategy.factory

import org.springframework.stereotype.Component
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.EMAIndicator
import org.ta4j.core.indicators.MACDIndicator
import org.ta4j.core.indicators.StochasticOscillatorKIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.CrossedDownIndicatorRule
import org.ta4j.core.rules.CrossedUpIndicatorRule
import org.ta4j.core.rules.OverIndicatorRule
import org.ta4j.core.rules.UnderIndicatorRule
import ru.kcheranev.trading.core.exception.StrategyParamValidationException
import ru.kcheranev.trading.core.strategy.factory.MovingMomentumLongStrategyParameter.EMA_MACD_BAR_COUNT
import ru.kcheranev.trading.core.strategy.factory.MovingMomentumLongStrategyParameter.LONG_EMA_BAR_COUNT
import ru.kcheranev.trading.core.strategy.factory.MovingMomentumLongStrategyParameter.LONG_MACD_BAR_COUNT
import ru.kcheranev.trading.core.strategy.factory.MovingMomentumLongStrategyParameter.SHORT_EMA_BAR_COUNT
import ru.kcheranev.trading.core.strategy.factory.MovingMomentumLongStrategyParameter.SHORT_MACD_BAR_COUNT
import ru.kcheranev.trading.core.strategy.factory.MovingMomentumLongStrategyParameter.STOCHASTIC_OSCILLATOR_K_BAR_COUNT
import ru.kcheranev.trading.domain.model.CustomizedBarSeries
import ru.kcheranev.trading.domain.model.StrategyParameter
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.TradeStrategy

@Component
class MovingMomentumLongStrategyFactory : LongStrategyFactory() {

    override fun initStrategy(
        parameters: StrategyParameters,
        series: CustomizedBarSeries
    ): TradeStrategy {
        val shortEmaBarCount = parameters.getAsInt(SHORT_EMA_BAR_COUNT)
        val longEmaBarCount = parameters.getAsInt(LONG_EMA_BAR_COUNT)
        val shortMacdBarCount = parameters.getAsInt(SHORT_MACD_BAR_COUNT)
        val longMacdBarCount = parameters.getAsInt(LONG_MACD_BAR_COUNT)
        val emaMacdBarCount = parameters.getAsInt(EMA_MACD_BAR_COUNT)
        val stochasticOscillatorKBarCount = parameters.getAsInt(STOCHASTIC_OSCILLATOR_K_BAR_COUNT)

        if (shortEmaBarCount >= longEmaBarCount) {
            throw StrategyParamValidationException("longEmaBarCount must be greater than shortEmaBarCount")
        }
        if (shortMacdBarCount >= longMacdBarCount) {
            throw StrategyParamValidationException("longMacdBarCount must be greater than shortMacdBarCount")
        }

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

        return buildTradeStrategy(series, BaseStrategy(entryRule, exitRule))
    }

    override fun strategyName() = "MOVING_MOMENTUM"

    override fun strategyParameterNames() = MovingMomentumLongStrategyParameter.values().map { it.alias() }
}

private enum class MovingMomentumLongStrategyParameter(private val alias: String) : StrategyParameter {

    SHORT_EMA_BAR_COUNT("shortEmaBarCount"),
    LONG_EMA_BAR_COUNT("longEmaBarCount"),
    SHORT_MACD_BAR_COUNT("shortMacdBarCount"),
    LONG_MACD_BAR_COUNT("longMacdBarCount"),
    EMA_MACD_BAR_COUNT("emaMacdBarCount"),
    STOCHASTIC_OSCILLATOR_K_BAR_COUNT("stochasticOscillatorKBarCount");

    override fun alias() = alias

}