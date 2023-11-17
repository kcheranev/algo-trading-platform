package ru.kcheranev.trading.core.service

import org.springframework.stereotype.Service
import ru.kcheranev.trading.core.port.outcome.broker.OrderServiceBrokerOutcomePort

@Service
class TradeService(
    private val orderServiceBrokerOutcomePort: OrderServiceBrokerOutcomePort
) {
}