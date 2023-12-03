package ru.kcheranev.trading.core.eventprocessor

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import ru.kcheranev.trading.common.LoggerDelegate
import ru.kcheranev.trading.core.port.income.trading.EnterTradeSessionCommand
import ru.kcheranev.trading.core.port.income.trading.EnterTradeSessionUseCase
import ru.kcheranev.trading.core.port.outcome.broker.OrderServiceBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.PostBestPriceBuyOrderCommand
import ru.kcheranev.trading.domain.TradeSessionPendedForEntryDomainEvent

@Component
class TradeSessionPendedForEntryDomainEventProcessor(
    private val orderServiceBrokerPort: OrderServiceBrokerPort,
    private val enterTradeSessionUseCase: EnterTradeSessionUseCase
) {

    @TransactionalEventListener
    fun processTradeSessionPendedForEntryDomainEvent(event: TradeSessionPendedForEntryDomainEvent) {
        logger.info("Trading session ${event.tradeSessionId} ${event.instrument.ticker} ${event.candleInterval} is ready for entry")
        orderServiceBrokerPort.postBestPriceBuyOrder(
            PostBestPriceBuyOrderCommand(event.instrument, event.lotsQuantity)
        )
        enterTradeSessionUseCase.enterTradeSession(EnterTradeSessionCommand(event.tradeSessionId))
    }

    companion object {

        private val logger by LoggerDelegate()

    }

}