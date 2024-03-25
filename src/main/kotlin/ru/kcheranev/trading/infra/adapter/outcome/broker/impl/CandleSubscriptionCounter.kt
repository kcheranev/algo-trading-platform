package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class CandleSubscriptionCounter {

    private val candleSubscriptions = ConcurrentHashMap<String, Int>()

    fun addCandleSubscription(key: String) {
        candleSubscriptions.merge(key, 1) { oldValue, value -> oldValue + value }
    }

    fun removeCandleSubscription(key: String) {
        candleSubscriptions.compute(key) { _, value -> if (value == 1) null else value?.minus(1) }
    }

    fun checkSubscriptionExists(key: String) = candleSubscriptions.containsKey(key)

    fun lastSubscription(key: String) = candleSubscriptions[key] == 1

    fun reset() {
        candleSubscriptions.clear()
    }

    fun getSubscriptions() = candleSubscriptions.toMap()

}