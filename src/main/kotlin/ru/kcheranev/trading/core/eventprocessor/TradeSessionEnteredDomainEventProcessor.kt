package ru.kcheranev.trading.core.eventprocessor

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import ru.kcheranev.trading.core.port.outcome.notification.NotificationPort
import ru.kcheranev.trading.core.port.outcome.notification.SendNotificationCommand
import ru.kcheranev.trading.domain.TradeSessionEnteredDomainEvent

@Component
class TradeSessionEnteredDomainEventProcessor(
    private val notificationPort: NotificationPort
) {

    @TransactionalEventListener
    fun processTradeSessionEnteredDomainEvent(event: TradeSessionEnteredDomainEvent) {
        with(event.tradeSession) {
            notificationPort.sendNotification(
                SendNotificationCommand(
                    "Trade session $about has been entered: lots requested ${event.lotsRequested}, lots executed ${currentPosition.lotsQuantity}"
                )
            )
        }
    }

}