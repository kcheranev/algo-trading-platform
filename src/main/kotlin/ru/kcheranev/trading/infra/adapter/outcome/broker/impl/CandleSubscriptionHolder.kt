package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import org.springframework.stereotype.Component
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.subscription.CandleSubscription
import java.util.concurrent.ConcurrentHashMap

@Component
class CandleSubscriptionHolder {

    private val candleSubscriptions = ConcurrentHashMap<String, CandleSubscription>()

    fun incrementSubscriptionCount(instrument: Instrument, candleInterval: CandleInterval) {
        val subscriptionId = CandleSubscription.candleSubscriptionId(instrument, candleInterval)
        candleSubscriptions.compute(subscriptionId) { _, value ->
            if (value == null) {
                CandleSubscription(instrument, candleInterval, 1)
            } else {
                CandleSubscription(instrument, candleInterval, value.subscriptionCount + 1)
            }
        }
    }

    fun removeCandleSubscription(subscriptionId: String) {
        candleSubscriptions.compute(subscriptionId) { _, value ->
            if (value == null || value.subscriptionCount == 1) {
                null
            } else {
                CandleSubscription(
                    value.instrument,
                    value.candleInterval,
                    value.subscriptionCount - 1
                )
            }
        }
    }

    fun checkSubscriptionExists(subscriptionId: String) =
        candleSubscriptions.containsKey(subscriptionId)

    fun isLastSubscription(subscriptionId: String) =
        candleSubscriptions[subscriptionId]?.subscriptionCount == 1

    fun reset() {
        candleSubscriptions.clear()
    }

    fun getSubscriptions() = candleSubscriptions.values.toList()

}