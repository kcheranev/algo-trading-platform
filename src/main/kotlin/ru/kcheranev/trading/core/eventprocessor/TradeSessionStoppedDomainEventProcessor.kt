package ru.kcheranev.trading.core.eventprocessor

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import ru.kcheranev.trading.common.LoggerDelegate
import ru.kcheranev.trading.core.port.outcome.broker.MarketDataStreamSubscriptionBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.UnsubscribeCandlesOrderCommand
import ru.kcheranev.trading.domain.TradeSessionStoppedDomainEvent

@Component
class TradeSessionStoppedDomainEventProcessor(
    private val marketDataStreamSubscriptionBrokerPort: MarketDataStreamSubscriptionBrokerPort
) {

    @TransactionalEventListener
    fun processTradeSessionStoppedDomainEvent(event: TradeSessionStoppedDomainEvent) {
        logger.info("Unsubscribe to the market data stream ${event.instrument.ticker} ${event.candleInterval}")
        marketDataStreamSubscriptionBrokerPort.unsubscribeCandles(
            UnsubscribeCandlesOrderCommand(event.instrument, event.candleInterval)
        )
    }

    companion object {

        private val logger by LoggerDelegate()

    }

}