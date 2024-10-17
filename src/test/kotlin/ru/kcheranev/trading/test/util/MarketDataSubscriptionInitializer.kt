package ru.kcheranev.trading.test.util

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.subscription.CandleSubscription
import ru.kcheranev.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionCacheHolder
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