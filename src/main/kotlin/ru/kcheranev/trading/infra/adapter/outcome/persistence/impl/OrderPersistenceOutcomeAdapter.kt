package ru.kcheranev.trading.infra.adapter.outcome.persistence.impl

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation.MANDATORY
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.port.outcome.persistence.GetOrderCommand
import ru.kcheranev.trading.core.port.outcome.persistence.OrderPersistencePort
import ru.kcheranev.trading.core.port.outcome.persistence.OrderSearchCommand
import ru.kcheranev.trading.core.port.outcome.persistence.SaveOrderCommand
import ru.kcheranev.trading.domain.entity.Order
import ru.kcheranev.trading.domain.entity.OrderId
import ru.kcheranev.trading.infra.adapter.outcome.persistence.OrderEntityNotExistsException
import ru.kcheranev.trading.infra.adapter.outcome.persistence.persistenceOutcomeAdapterMapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.OrderRepository

@Component
class OrderPersistenceOutcomeAdapter(
    private val orderRepository: OrderRepository
) : OrderPersistencePort {

    @Transactional(propagation = MANDATORY)
    override fun save(command: SaveOrderCommand): OrderId {
        return OrderId(orderRepository.save(persistenceOutcomeAdapterMapper.map(command.order)).id!!)
    }

    override fun get(command: GetOrderCommand): Order {
        return orderRepository.findById(command.orderId.value)
            .orElseThrow { OrderEntityNotExistsException(command.orderId) }
            .let { persistenceOutcomeAdapterMapper.map(it) }
    }

    override fun search(command: OrderSearchCommand): List<Order> =
        orderRepository.search(command).map { persistenceOutcomeAdapterMapper.map(it) }

}