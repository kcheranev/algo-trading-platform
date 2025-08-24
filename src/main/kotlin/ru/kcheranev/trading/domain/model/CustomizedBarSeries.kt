package ru.kcheranev.trading.domain.model

import org.ta4j.core.BarSeries

class CustomizedBarSeries(barSeries: BarSeries) : BarSeries by barSeries