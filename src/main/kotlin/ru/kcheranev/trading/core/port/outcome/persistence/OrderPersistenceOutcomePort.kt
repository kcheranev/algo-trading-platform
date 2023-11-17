package ru.kcheranev.trading.core.port.outcome.persistence

import ru.kcheranev.trading.domain.entity.Order
import ru.kcheranev.trading.domain.entity.OrderId

interface OrderPersistenceOutcomePort {

    fun save(command: SaveOrderPersistenceOutcomeCommand): OrderId

    fun get(command: GetOrderPersistenceOutcomeCommand): Order

}