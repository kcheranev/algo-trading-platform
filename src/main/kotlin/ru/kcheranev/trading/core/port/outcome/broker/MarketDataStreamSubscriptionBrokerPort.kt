package ru.kcheranev.trading.core.port.outcome.broker

import ru.kcheranev.trading.domain.model.subscription.CandleSubscription

interface MarketDataStreamSubscriptionBrokerPort {

    fun subscribeCandles(command: SubscribeCandlesOrderCommand)

    fun unsubscribeCandles(command: UnsubscribeCandlesOrderCommand)

    fun findAllCandleSubscriptions(): Set<CandleSubscription>

}