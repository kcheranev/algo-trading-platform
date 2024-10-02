package ru.kcheranev.trading.test.strategy

import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.CrossedDownIndicatorRule
import org.ta4j.core.rules.CrossedUpIndicatorRule
import ru.kcheranev.trading.core.strategy.factory.LongStrategyFactory
import ru.kcheranev.trading.domain.model.CustomizedBarSeries
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.TradeStrategy
import java.math.BigDecimal

class DummyTestLongStrategyFactory : LongStrategyFactory() {

    override fun initStrategy(
        parameters: StrategyParameters,
        series: CustomizedBarSeries
    ): TradeStrategy {
        val closePrice = ClosePriceIndicator(series)
        val entryRule = CrossedUpIndicatorRule(closePrice, BigDecimal(100))
        val exitRule =
            CrossedUpIndicatorRule(closePrice, BigDecimal(105))
                .or(CrossedDownIndicatorRule(closePrice, BigDecimal(98)))
        return buildTradeStrategy(series, BaseStrategy(entryRule, exitRule, 4))
    }

    override fun strategyName() = "DUMMY"

    override fun strategyParameterNames() = emptyList<String>()

}