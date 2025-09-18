package com.github.trading.core.eventprocessor

import com.github.trading.core.port.outcome.broker.MarketDataStreamSubscriptionBrokerPort
import com.github.trading.core.port.outcome.broker.UnsubscribeCandlesOrderCommand
import com.github.trading.domain.TradeSessionStoppedDomainEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class TradeSessionStoppedDomainEventProcessor(
    private val marketDataStreamSubscriptionBrokerPort: MarketDataStreamSubscriptionBrokerPort
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener
    fun processTradeSessionStoppedDomainEvent(event: TradeSessionStoppedDomainEvent) {
        with(event.tradeSession) {
            log.info("Unsubscribe to the market data stream ticker=$ticker, candleInterval=$candleInterval")
            marketDataStreamSubscriptionBrokerPort.unsubscribeCandles(
                UnsubscribeCandlesOrderCommand(
                    instrument,
                    candleInterval
                )
            )
        }
    }

}