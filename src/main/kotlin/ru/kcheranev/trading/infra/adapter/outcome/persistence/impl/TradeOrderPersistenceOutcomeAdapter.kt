package ru.kcheranev.trading.infra.adapter.outcome.persistence.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation.MANDATORY
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.port.outcome.persistence.GetOrderCommand
import ru.kcheranev.trading.core.port.outcome.persistence.SaveOrderCommand
import ru.kcheranev.trading.core.port.outcome.persistence.TradeOrderPersistencePort
import ru.kcheranev.trading.core.port.outcome.persistence.TradeOrderSearchCommand
import ru.kcheranev.trading.domain.entity.TradeOrderId
import ru.kcheranev.trading.infra.adapter.outcome.PersistenceOutcomeAdapterException
import ru.kcheranev.trading.infra.adapter.outcome.persistence.persistenceOutcomeAdapterMapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.TradeOrderRepository

@Component
class TradeOrderPersistenceOutcomeAdapter(
    private val tradeOrderRepository: TradeOrderRepository,
    private val eventPublisher: ApplicationEventPublisher
) : TradeOrderPersistencePort {

    @Transactional(propagation = MANDATORY)
    override fun save(command: SaveOrderCommand): TradeOrderId {
        val tradeOrder = command.tradeOrder
        val tradeOrderId = TradeOrderId(tradeOrderRepository.save(persistenceOutcomeAdapterMapper.map(tradeOrder)).id!!)
        tradeOrder.events.forEach { eventPublisher.publishEvent(it) }
        tradeOrder.clearEvents()
        return tradeOrderId
    }

    override fun get(command: GetOrderCommand) =
        tradeOrderRepository.findById(command.tradeOrderId.value)
            .orElseThrow {
                PersistenceOutcomeAdapterException("Trade order entity with id ${command.tradeOrderId.value} is not exists")
            }
            .let { persistenceOutcomeAdapterMapper.map(it) }


    override fun search(command: TradeOrderSearchCommand) =
        tradeOrderRepository.search(command).map { persistenceOutcomeAdapterMapper.map(it) }

}