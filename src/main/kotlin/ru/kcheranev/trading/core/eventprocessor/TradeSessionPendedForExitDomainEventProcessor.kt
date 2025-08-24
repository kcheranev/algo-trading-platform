package ru.kcheranev.trading.core.eventprocessor

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import ru.kcheranev.trading.core.port.income.tradesession.ExitTradeSessionCommand
import ru.kcheranev.trading.core.port.income.tradesession.ExitTradeSessionUseCase
import ru.kcheranev.trading.domain.TradeSessionPendedForExitDomainEvent

@Component
class TradeSessionPendedForExitDomainEventProcessor(
    private val exitTradeSessionUseCase: ExitTradeSessionUseCase
) {

    @TransactionalEventListener
    fun processTradeSessionPendedForExitDomainEvent(event: TradeSessionPendedForExitDomainEvent) {
        exitTradeSessionUseCase.exitTradeSession(ExitTradeSessionCommand(event.tradeSession.id))
    }

}