package ru.kcheranev.trading.test.util

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionHolder
import ru.tinkoff.piapi.core.stream.MarketDataStreamService

class MarketDataSubscriptionInitializer(
    private val candleSubscriptionHolder: CandleSubscriptionHolder,
    private val marketDataStreamService: MarketDataStreamService
) {

    fun init(instrument: Instrument, candleInterval: CandleInterval) {
        val subscriptionKey = "candles_${instrument.ticker}_$candleInterval"
        candleSubscriptionHolder.incrementSubscriptionCount(instrument, candleInterval)
        marketDataStreamService.newStream(subscriptionKey, {}) {}
    }

}