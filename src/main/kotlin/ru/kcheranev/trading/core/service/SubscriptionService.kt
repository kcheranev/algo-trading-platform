package ru.kcheranev.trading.core.service

import org.springframework.stereotype.Service
import ru.kcheranev.trading.core.port.income.subscription.SearchSubscriptionUseCase
import ru.kcheranev.trading.core.port.outcome.broker.MarketDataStreamSubscriptionBrokerPort

@Service
class SubscriptionService(
    private val marketDataStreamSubscriptionBrokerPort: MarketDataStreamSubscriptionBrokerPort
) : SearchSubscriptionUseCase {

    override fun findAllCandleSubscriptions() = marketDataStreamSubscriptionBrokerPort.findAllCandleSubscriptions()

}