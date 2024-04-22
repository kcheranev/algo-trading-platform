package ru.kcheranev.trading.core.strategy.factory

import ru.kcheranev.trading.domain.model.CustomizedBarSeries
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.TradeStrategy

interface StrategyFactory {

    fun initStrategy(
        params: StrategyParameters,
        series: CustomizedBarSeries
    ): TradeStrategy

    fun strategyType(): String

}