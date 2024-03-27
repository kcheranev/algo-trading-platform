package ru.kcheranev.trading.core.eventprocessor

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import ru.kcheranev.trading.core.port.income.trading.EnterTradeSessionCommand
import ru.kcheranev.trading.core.port.income.trading.EnterTradeSessionUseCase
import ru.kcheranev.trading.domain.TradeSessionPendedForEntryDomainEvent

@Component
class TradeSessionPendedForEntryDomainEventProcessor(
    private val enterTradeSessionUseCase: EnterTradeSessionUseCase,
) {

    @TransactionalEventListener
    fun processTradeSessionPendedForEntryDomainEvent(event: TradeSessionPendedForEntryDomainEvent) {
        enterTradeSessionUseCase.enterTradeSession(EnterTradeSessionCommand(event.tradeSessionId))
    }

}