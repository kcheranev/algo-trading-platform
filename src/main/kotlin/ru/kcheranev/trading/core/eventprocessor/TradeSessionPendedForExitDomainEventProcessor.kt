package ru.kcheranev.trading.core.eventprocessor

import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import ru.kcheranev.trading.common.LoggerDelegate
import ru.kcheranev.trading.core.port.income.trading.ExitTradeSessionCommand
import ru.kcheranev.trading.core.port.income.trading.ExitTradeSessionUseCase
import ru.kcheranev.trading.core.port.outcome.broker.OrderServiceBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.PostBestPriceSellOrderCommand
import ru.kcheranev.trading.domain.TradeSessionPendedForExitDomainEvent

@Component
class TradeSessionPendedForExitDomainEventProcessor(
    private val orderServiceBrokerPort: OrderServiceBrokerPort,
    private val exitTradeSessionUseCase: ExitTradeSessionUseCase
) {

    @TransactionalEventListener
    fun processTradeSessionPendedForExitDomainEvent(event: TradeSessionPendedForExitDomainEvent) {
        logger.info("Trading session ${event.tradeSessionId} ${event.instrument.ticker} ${event.candleInterval} is ready for exit")
        orderServiceBrokerPort.postBestPriceSellOrder(
            PostBestPriceSellOrderCommand(event.instrument, event.lotsQuantity)
        )
        exitTradeSessionUseCase.exitTradeSession(ExitTradeSessionCommand(event.tradeSessionId))
    }

    companion object {

        private val logger by LoggerDelegate()

    }

}