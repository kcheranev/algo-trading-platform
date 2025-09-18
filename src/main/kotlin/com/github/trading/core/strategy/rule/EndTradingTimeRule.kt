package com.github.trading.core.strategy.rule

import com.github.trading.domain.model.CandleInterval
import org.ta4j.core.TradingRecord
import org.ta4j.core.indicators.helpers.DateTimeIndicator
import org.ta4j.core.rules.AbstractRule
import java.time.LocalTime

class EndTradingTimeRule(
    private val endTradingTime: LocalTime,
    private val candleInterval: CandleInterval,
    private val timeIndicator: DateTimeIndicator
) : AbstractRule() {

    override fun isSatisfied(index: Int, tradingRecord: TradingRecord?) =
        timeIndicator.getValue(index).toLocalTime() > endTradingTime - candleInterval.duration.multipliedBy(2)

}