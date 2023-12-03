package ru.kcheranev.trading.core.eventprocessor

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import ru.kcheranev.trading.common.LoggerDelegate
import ru.kcheranev.trading.core.port.outcome.broker.MarketDataStreamSubscriptionBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.SubscribeCandlesOrderCommand
import ru.kcheranev.trading.domain.TradeSessionCreatedDomainEvent

@Component
class TradeSessionCreatedDomainEventProcessor(
    private val marketDataStreamSubscriptionBrokerPort: MarketDataStreamSubscriptionBrokerPort
) {

    @TransactionalEventListener
    fun processTradeSessionCreatedDomainEvent(event: TradeSessionCreatedDomainEvent) {
        logger.info("Subscribe to the market data stream ${event.instrument.ticker} ${event.candleInterval}")
        marketDataStreamSubscriptionBrokerPort.subscribeCandles(
            SubscribeCandlesOrderCommand(event.instrument, event.candleInterval)
        )
    }

    companion object {

        private val logger by LoggerDelegate()

    }

}