package ru.kcheranev.trading.core.strategy

import org.ta4j.core.BarSeries
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.StrategyType

interface StrategyFactory {

    fun initStrategy(
        params: StrategyParameters,
        series: BarSeries
    ): TradeStrategy

    fun strategyType(): StrategyType

}