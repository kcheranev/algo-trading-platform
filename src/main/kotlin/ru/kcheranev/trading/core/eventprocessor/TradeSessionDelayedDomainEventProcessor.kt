package ru.kcheranev.trading.core.eventprocessor

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import ru.kcheranev.trading.core.port.income.tradesession.ReinitStrategyCommand
import ru.kcheranev.trading.core.port.income.tradesession.ResumeTradeSessionUseCase
import ru.kcheranev.trading.domain.TradeSessionDelayedDomainEvent

@Component
class TradeSessionDelayedDomainEventProcessor(
    private val resumeTradeSessionUseCase: ResumeTradeSessionUseCase
) {

    @TransactionalEventListener
    fun processTradeSessionDelayedDomainEvent(event: TradeSessionDelayedDomainEvent) {
        resumeTradeSessionUseCase.reinitStrategy(ReinitStrategyCommand(event.tradeSessionId))
    }

}