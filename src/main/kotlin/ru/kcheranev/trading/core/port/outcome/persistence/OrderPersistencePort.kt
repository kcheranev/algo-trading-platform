package ru.kcheranev.trading.core.port.outcome.persistence

import ru.kcheranev.trading.domain.entity.Order
import ru.kcheranev.trading.domain.entity.OrderId

interface OrderPersistencePort {

    fun save(command: SaveOrderCommand): OrderId

    fun get(command: GetOrderCommand): Order

    fun search(command: OrderSearchCommand): List<Order>

}