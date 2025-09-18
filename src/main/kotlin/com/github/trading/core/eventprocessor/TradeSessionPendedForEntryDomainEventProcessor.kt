package com.github.trading.core.eventprocessor

import com.github.trading.core.port.income.tradesession.EnterTradeSessionCommand
import com.github.trading.core.port.income.tradesession.EnterTradeSessionUseCase
import com.github.trading.domain.TradeSessionPendedForEntryDomainEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class TradeSessionPendedForEntryDomainEventProcessor(
    private val enterTradeSessionUseCase: EnterTradeSessionUseCase
) {

    @TransactionalEventListener
    fun processTradeSessionPendedForEntryDomainEvent(event: TradeSessionPendedForEntryDomainEvent) {
        enterTradeSessionUseCase.enterTradeSession(EnterTradeSessionCommand(event.tradeSession.id))
    }

}