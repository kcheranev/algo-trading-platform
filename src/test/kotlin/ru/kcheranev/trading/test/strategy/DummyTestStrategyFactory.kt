package ru.kcheranev.trading.test.strategy

import org.ta4j.core.BarSeries
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.CrossedUpIndicatorRule
import ru.kcheranev.trading.core.strategy.StrategyFactory
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.TradeStrategy
import java.math.BigDecimal

class DummyTestStrategyFactory : StrategyFactory {

    override fun initStrategy(
        params: StrategyParameters,
        series: BarSeries
    ): TradeStrategy {
        val closePrice = ClosePriceIndicator(series)
        val entryRule = CrossedUpIndicatorRule(closePrice, BigDecimal(100))
        val exitRule = CrossedUpIndicatorRule(closePrice, BigDecimal(105))
        return TradeStrategy(series, BaseStrategy(entryRule, exitRule))
    }

    override fun strategyType() = "DUMMY"

}