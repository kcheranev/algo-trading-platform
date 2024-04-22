package ru.kcheranev.trading.domain.model

import org.ta4j.core.BarSeries

class CustomizedBarSeries(
    barSeries: BarSeries,
    val candleInterval: CandleInterval
) : BarSeries by barSeries