package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class CandleSubscriptionCounter {

    private val candleSubscriptions = ConcurrentHashMap<String, Int>()

    fun addCandleSubscription(subscriptionKey: String) {
        candleSubscriptions.merge(subscriptionKey, 1) { oldValue, value -> oldValue + value }
    }

    fun removeCandleSubscription(subscriptionKey: String) {
        candleSubscriptions.compute(subscriptionKey) { _, value -> if (value == 1) null else value?.minus(1) }
    }

    fun checkSubscriptionExists(subscriptionKey: String) = candleSubscriptions.containsKey(subscriptionKey)

    fun reset() {
        candleSubscriptions.clear()
    }

}