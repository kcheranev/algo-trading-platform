package ru.kcheranev.trading.core.strategy

import org.ta4j.core.Bar
import org.ta4j.core.BarSeries
import org.ta4j.core.Strategy

class TradeStrategy(
    private val series: BarSeries,
    strategy: Strategy
) : Strategy by strategy {

    fun addBar(bar: Bar) {
        series.addBar(bar)
    }

}