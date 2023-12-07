package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom

import ru.kcheranev.trading.core.port.outcome.persistence.OrderSearchCommand
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.OrderEntity

interface CustomizedOrderRepository {

    fun search(command: OrderSearchCommand): List<OrderEntity>

}