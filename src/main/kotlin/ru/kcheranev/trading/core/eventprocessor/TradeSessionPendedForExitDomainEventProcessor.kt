package ru.kcheranev.trading.core.eventprocessor

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
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

    private val log = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener
    fun processTradeSessionPendedForExitDomainEvent(event: TradeSessionPendedForExitDomainEvent) {
        log.info("Trading session ${event.tradeSessionId} ${event.instrument.ticker} ${event.candleInterval} is ready for exit")
        val postOrderResponse =
            orderServiceBrokerPort.postBestPriceSellOrderSync(
                PostBestPriceSellOrderCommand(event.instrument, event.lotsQuantity)
            )
        exitTradeSessionUseCase.exitTradeSession(
            with(postOrderResponse) {
                ExitTradeSessionCommand(
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