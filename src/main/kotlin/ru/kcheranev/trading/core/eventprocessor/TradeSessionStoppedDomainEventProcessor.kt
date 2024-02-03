package ru.kcheranev.trading.core.eventprocessor

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import ru.kcheranev.trading.core.port.outcome.broker.MarketDataStreamSubscriptionBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.UnsubscribeCandlesOrderCommand
import ru.kcheranev.trading.domain.TradeSessionStoppedDomainEvent

@Component
class TradeSessionStoppedDomainEventProcessor(
    private val marketDataStreamSubscriptionBrokerPort: MarketDataStreamSubscriptionBrokerPort
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener
    fun processTradeSessionStoppedDomainEvent(event: TradeSessionStoppedDomainEvent) {
        log.info(
            "Unsubscribe to the market data stream ticker=${event.instrument.ticker}, " +
                    "candleInterval=${event.candleInterval}"
        )
        marketDataStreamSubscriptionBrokerPort.unsubscribeCandles(
            UnsubscribeCandlesOrderCommand(event.instrument, event.candleInterval)
        )
    }

}