package ru.kcheranev.trading.test.util

import io.mockk.mockk
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.TradeStrategy
import ru.kcheranev.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionCounter
import ru.kcheranev.trading.infra.adapter.outcome.persistence.impl.TradeStrategyCache
import ru.tinkoff.piapi.core.stream.MarketDataStreamService

class TradeSessionContextInitializer(
    private val tradeStrategyCache: TradeStrategyCache,
    private val candleSubscriptionCounter: CandleSubscriptionCounter,
    private val marketDataStreamService: MarketDataStreamService
) {

    fun init(
        tradeSessionId: Long,
        ticker: String,
        candleInterval: CandleInterval,
        tradeStrategy: TradeStrategy = mockk()
    ) {
        val subscriptionKey = "candles_${ticker}_$candleInterval"
        tradeStrategyCache.put(tradeSessionId, tradeStrategy)
        candleSubscriptionCounter.addCandleSubscription(subscriptionKey)
        marketDataStreamService.newStream(subscriptionKey, {}) {}
    }

}