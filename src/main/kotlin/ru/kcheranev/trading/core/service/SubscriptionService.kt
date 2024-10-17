package ru.kcheranev.trading.core.service

import org.springframework.stereotype.Service
import ru.kcheranev.trading.core.port.income.subscription.RefreshCandleSubscriptionsUseCase
import ru.kcheranev.trading.core.port.income.subscription.SearchCandleSubscriptionUseCase
import ru.kcheranev.trading.core.port.outcome.broker.MarketDataStreamSubscriptionBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.SubscribeCandlesOrderCommand
import ru.kcheranev.trading.core.port.outcome.broker.UnsubscribeCandlesOrderCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.TradeSessionPersistencePort
import ru.kcheranev.trading.domain.model.subscription.CandleSubscription

@Service
class SubscriptionService(
    private val tradeSessionPersistencePort: TradeSessionPersistencePort,
    private val marketDataStreamSubscriptionBrokerPort: MarketDataStreamSubscriptionBrokerPort
) : SearchCandleSubscriptionUseCase,
    RefreshCandleSubscriptionsUseCase {

    override fun refreshCandleSubscriptions() {
        val activeCandleSubscriptions = marketDataStreamSubscriptionBrokerPort.findAllCandleSubscriptions()
        val expectedCandleSubscriptions =
            tradeSessionPersistencePort.getReadyForOrderTradeSessions()
                .map { CandleSubscription(it.instrument, it.candleInterval) }
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
                    SubscribeCandlesOrderCommand(it.instrument, it.candleInterval)
                )
            }
    }

    override fun findAllCandleSubscriptions() = marketDataStreamSubscriptionBrokerPort.findAllCandleSubscriptions()

}