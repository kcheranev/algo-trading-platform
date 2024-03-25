package ru.kcheranev.trading.test.util

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionCounter
import ru.tinkoff.piapi.core.stream.MarketDataStreamService

class MarketDataSubscriptionInitializer(
    private val candleSubscriptionCounter: CandleSubscriptionCounter,
    private val marketDataStreamService: MarketDataStreamService
) {

    fun init(ticker: String, candleInterval: CandleInterval) {
        val subscriptionKey = "candles_${ticker}_$candleInterval"
        candleSubscriptionCounter.addCandleSubscription(subscriptionKey)
        marketDataStreamService.newStream(subscriptionKey, {}) {}
    }

}