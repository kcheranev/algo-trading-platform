package com.github.trading.infra.adapter.outcome.broker.impl

import com.github.trading.domain.model.subscription.CandleSubscription
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class CandleSubscriptionCacheHolder {

    private val candleSubscriptions = ConcurrentHashMap.newKeySet<CandleSubscription>()

    fun add(candleSubscription: CandleSubscription) {
        candleSubscriptions.add(candleSubscription)
    }

    fun remove(candleSubscription: CandleSubscription) {
        candleSubscriptions.remove(candleSubscription)
    }

    fun contains(candleSubscription: CandleSubscription) = candleSubscriptions.contains(candleSubscription)

    fun findAll(): Set<CandleSubscription> = candleSubscriptions

    fun clear() {
        candleSubscriptions.clear()
    }

}