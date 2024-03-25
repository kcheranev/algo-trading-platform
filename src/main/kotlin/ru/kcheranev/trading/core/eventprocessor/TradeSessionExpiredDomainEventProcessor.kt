package ru.kcheranev.trading.core.eventprocessor

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import ru.kcheranev.trading.core.port.outcome.broker.MarketDataStreamSubscriptionBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.UnsubscribeCandlesOrderCommand
import ru.kcheranev.trading.core.port.outcome.notification.NotificationPort
import ru.kcheranev.trading.core.port.outcome.notification.SendNotificationCommand
import ru.kcheranev.trading.domain.TradeSessionExpiredDomainEvent

@Component
class TradeSessionExpiredDomainEventProcessor(
    private val marketDataStreamSubscriptionBrokerPort: MarketDataStreamSubscriptionBrokerPort,
    private val notificationPort: NotificationPort
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener
    fun processTradeSessionExpiredDomainEvent(event: TradeSessionExpiredDomainEvent) {
        log.info(
            "Unsubscribe to the market data stream ticker=${event.instrument.ticker}, " +
                    "candleInterval=${event.candleInterval}"
        )
        marketDataStreamSubscriptionBrokerPort.unsubscribeCandles(
            UnsubscribeCandlesOrderCommand(event.instrument, event.candleInterval)
        )
        notificationPort.sendNotification(
            SendNotificationCommand(
                "Trade session ticker=${event.instrument.ticker} " +
                        "candleInterval=${event.candleInterval} has been expired"
            )
        )
    }

}