package ru.kcheranev.trading.core.port.income.search

import ru.kcheranev.trading.domain.entity.Order

interface OrderSearchUseCase {

    fun search(command: OrderSearchCommand): List<Order>

}