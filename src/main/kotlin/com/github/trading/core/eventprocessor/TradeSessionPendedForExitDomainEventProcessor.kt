package com.github.trading.core.eventprocessor

import com.github.trading.core.port.income.tradesession.ExitTradeSessionCommand
import com.github.trading.core.port.income.tradesession.ExitTradeSessionUseCase
import com.github.trading.domain.TradeSessionPendedForExitDomainEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class TradeSessionPendedForExitDomainEventProcessor(
    private val exitTradeSessionUseCase: ExitTradeSessionUseCase
) {

    @TransactionalEventListener
    fun processTradeSessionPendedForExitDomainEvent(event: TradeSessionPendedForExitDomainEvent) {
        exitTradeSessionUseCase.exitTradeSession(ExitTradeSessionCommand(event.tradeSession.id))
    }

}