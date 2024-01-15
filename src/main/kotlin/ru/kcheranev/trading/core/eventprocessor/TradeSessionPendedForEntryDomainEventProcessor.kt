package ru.kcheranev.trading.core.eventprocessor

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
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

    private val log = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener
    fun processTradeSessionPendedForEntryDomainEvent(event: TradeSessionPendedForEntryDomainEvent) {
        log.info("Trading session ${event.tradeSessionId} ${event.instrument.ticker} ${event.candleInterval} is ready for entry")
        val postOrderResponse =
            orderServiceBrokerPort.postBestPriceBuyOrderSync(
                PostBestPriceBuyOrderCommand(event.instrument, event.lotsQuantity)
            )
        enterTradeSessionUseCase.enterTradeSession(
            with(postOrderResponse) {
                EnterTradeSessionCommand(
                    event.tradeSessionId,
                    status = status,
                    lotsRequested = lotsRequested,
                    lotsExecuted = lotsExecuted,
                    totalPrice = totalPrice,
                    executedCommission = executedCommission
                )
            }
        )
    }

}