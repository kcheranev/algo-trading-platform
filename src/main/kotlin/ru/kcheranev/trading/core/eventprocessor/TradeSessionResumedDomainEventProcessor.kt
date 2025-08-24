package ru.kcheranev.trading.core.eventprocessor

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import ru.kcheranev.trading.core.port.outcome.broker.MarketDataStreamSubscriptionBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.SubscribeCandlesOrderCommand
import ru.kcheranev.trading.domain.TradeSessionResumedDomainEvent

@Component
class TradeSessionResumedDomainEventProcessor(
    private val marketDataStreamSubscriptionBrokerPort: MarketDataStreamSubscriptionBrokerPort
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener
    fun processTradeSessionResumedDomainEvent(event: TradeSessionResumedDomainEvent) {
        with(event.tradeSession) {
            log.info("Subscribe to the market data stream ticker=${instrument.ticker}, candleInterval=$candleInterval")
            marketDataStreamSubscriptionBrokerPort.subscribeCandles(
                SubscribeCandlesOrderCommand(instrument, candleInterval)
            )
        }
    }

}