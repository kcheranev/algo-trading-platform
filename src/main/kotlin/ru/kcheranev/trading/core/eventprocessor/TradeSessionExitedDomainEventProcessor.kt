package ru.kcheranev.trading.core.eventprocessor

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import ru.kcheranev.trading.core.port.outcome.notification.NotificationPort
import ru.kcheranev.trading.core.port.outcome.notification.SendNotificationCommand
import ru.kcheranev.trading.domain.TradeSessionExitedDomainEvent

@Component
class TradeSessionExitedDomainEventProcessor(
    private val notificationPort: NotificationPort
) {

    @TransactionalEventListener
    fun processTradeSessionExitedDomainEvent(event: TradeSessionExitedDomainEvent) {
        with(event) {
            notificationPort.sendNotification(
                SendNotificationCommand(
                    "Trade session ${tradeSession.about} has been exited: lots requested $lotsRequested, lots executed $lotsExecuted"
                )
            )
        }
    }

}