package ru.kcheranev.trading.core.port.outcome.broker

interface MarketDataStreamSubscriptionBrokerPort {

    fun subscribeCandles(command: SubscribeCandlesOrderCommand)

    fun unsubscribeCandles(command: UnsubscribeCandlesOrderCommand)

}