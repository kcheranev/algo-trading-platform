package com.github.trading.core.eventprocessor

import com.github.trading.core.port.outcome.notification.NotificationPort
import com.github.trading.core.port.outcome.notification.SendNotificationCommand
import com.github.trading.domain.TradeSessionEnteredDomainEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

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