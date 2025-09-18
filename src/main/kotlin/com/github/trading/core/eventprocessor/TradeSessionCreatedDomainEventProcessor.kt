package com.github.trading.core.eventprocessor

import com.github.trading.core.port.outcome.broker.MarketDataStreamSubscriptionBrokerPort
import com.github.trading.core.port.outcome.broker.SubscribeCandlesOrderCommand
import com.github.trading.domain.TradeSessionCreatedDomainEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class TradeSessionCreatedDomainEventProcessor(
    private val marketDataStreamSubscriptionBrokerPort: MarketDataStreamSubscriptionBrokerPort
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener
    fun processTradeSessionCreatedDomainEvent(event: TradeSessionCreatedDomainEvent) {
        with(event.tradeSession) {
            log.info("Subscribe to the market data stream ticker=${instrument.ticker}, candleInterval=$candleInterval")
            marketDataStreamSubscriptionBrokerPort.subscribeCandles(
                SubscribeCandlesOrderCommand(instrument, candleInterval)
            )
        }
    }

}