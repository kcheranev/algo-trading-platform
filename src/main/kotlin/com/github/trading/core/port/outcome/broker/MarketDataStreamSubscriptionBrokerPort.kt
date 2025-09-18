package com.github.trading.core.port.outcome.broker

import com.github.trading.domain.model.subscription.CandleSubscription

interface MarketDataStreamSubscriptionBrokerPort {

    fun subscribeCandles(command: SubscribeCandlesOrderCommand)

    fun unsubscribeCandles(command: UnsubscribeCandlesOrderCommand)

    fun findAllCandleSubscriptions(): Set<CandleSubscription>

}