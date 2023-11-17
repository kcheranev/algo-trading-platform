package ru.kcheranev.trading.domain.model

import org.ta4j.core.Bar
import org.ta4j.core.BarSeries
import org.ta4j.core.Strategy

class TradeStrategy(
    val series: BarSeries,
    strategy: Strategy
) : Strategy by strategy {

    fun addBar(bar: Bar) {
        series.addBar(bar)
    }

}