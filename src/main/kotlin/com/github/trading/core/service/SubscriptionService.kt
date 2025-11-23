package com.github.trading.core.service

import com.github.trading.core.port.income.subscription.RefreshCandleSubscriptionsUseCase
import com.github.trading.core.port.income.subscription.SearchCandleSubscriptionUseCase
import com.github.trading.core.port.outcome.broker.MarketDataStreamSubscriptionBrokerPort
import com.github.trading.core.port.outcome.broker.SubscribeCandlesCommand
import com.github.trading.core.port.outcome.broker.UnsubscribeCandlesOrderCommand
import com.github.trading.core.port.outcome.persistence.tradesession.TradeSessionPersistencePort
import org.springframework.stereotype.Service

@Service
class SubscriptionService(
    private val tradeSessionPersistencePort: TradeSessionPersistencePort,
    private val marketDataStreamSubscriptionBrokerPort: MarketDataStreamSubscriptionBrokerPort
) : SearchCandleSubscriptionUseCase,
    RefreshCandleSubscriptionsUseCase {

    override fun refreshCandleSubscriptions() {
        val activeCandleSubscriptions = marketDataStreamSubscriptionBrokerPort.findAllCandleSubscriptions()
        val expectedCandleSubscriptions =
            tradeSessionPersistencePort.getActiveCandleSubscriptions()
                .toSet()
        activeCandleSubscriptions.minus(expectedCandleSubscriptions)
            .forEach {
                marketDataStreamSubscriptionBrokerPort.unsubscribeCandles(
                    UnsubscribeCandlesOrderCommand(it.instrument, it.candleInterval)
                )
            }
        expectedCandleSubscriptions.minus(activeCandleSubscriptions)
            .forEach {
                marketDataStreamSubscriptionBrokerPort.subscribeCandles(
                    SubscribeCandlesCommand(it.instrument, it.candleInterval)
                )
            }
    }

    override fun findAllCandleSubscriptions() = marketDataStreamSubscriptionBrokerPort.findAllCandleSubscriptions()

}