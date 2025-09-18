package com.github.trading.test.util

import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.Instrument
import com.github.trading.domain.model.subscription.CandleSubscription
import com.github.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionCacheHolder
import ru.tinkoff.piapi.core.stream.MarketDataStreamService

class MarketDataSubscriptionInitializer(
    private val candleSubscriptionCacheHolder: CandleSubscriptionCacheHolder,
    private val marketDataStreamService: MarketDataStreamService
) {

    fun addSubscription(instrument: Instrument, candleInterval: CandleInterval) {
        val subscriptionKey = "candles_${instrument.ticker}_$candleInterval"
        candleSubscriptionCacheHolder.add(CandleSubscription(instrument, candleInterval))
        marketDataStreamService.newStream(subscriptionKey, {}) {}
    }

}