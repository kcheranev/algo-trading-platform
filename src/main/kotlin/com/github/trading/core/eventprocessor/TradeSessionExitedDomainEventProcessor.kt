package com.github.trading.core.eventprocessor

import com.github.trading.core.port.outcome.notification.NotificationPort
import com.github.trading.core.port.outcome.notification.SendNotificationCommand
import com.github.trading.domain.TradeSessionExitedDomainEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

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