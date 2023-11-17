package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.OrderEntity

interface OrderRepository : CrudRepository<OrderEntity, Long> {
}