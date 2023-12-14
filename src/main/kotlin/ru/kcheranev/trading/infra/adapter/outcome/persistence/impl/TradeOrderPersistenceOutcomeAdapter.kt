package ru.kcheranev.trading.infra.adapter.outcome.persistence.impl

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation.MANDATORY
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.port.outcome.persistence.GetOrderCommand
import ru.kcheranev.trading.core.port.outcome.persistence.SaveOrderCommand
import ru.kcheranev.trading.core.port.outcome.persistence.TradeOrderPersistencePort
import ru.kcheranev.trading.core.port.outcome.persistence.TradeOrderSearchCommand
import ru.kcheranev.trading.domain.entity.TradeOrder
import ru.kcheranev.trading.domain.entity.TradeOrderId
import ru.kcheranev.trading.infra.adapter.outcome.persistence.TradeOrderEntityNotExistsException
import ru.kcheranev.trading.infra.adapter.outcome.persistence.persistenceOutcomeAdapterMapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.TradeOrderRepository

@Component
class TradeOrderPersistenceOutcomeAdapter(
    private val tradeOrderRepository: TradeOrderRepository
) : TradeOrderPersistencePort {

    @Transactional(propagation = MANDATORY)
    override fun save(command: SaveOrderCommand): TradeOrderId {
        return TradeOrderId(tradeOrderRepository.save(persistenceOutcomeAdapterMapper.map(command.tradeOrder)).id!!)
    }

    override fun get(command: GetOrderCommand): TradeOrder {
        return tradeOrderRepository.findById(command.tradeOrderId.value)
            .orElseThrow { TradeOrderEntityNotExistsException(command.tradeOrderId) }
            .let { persistenceOutcomeAdapterMapper.map(it) }
    }

    override fun search(command: TradeOrderSearchCommand): List<TradeOrder> =
        tradeOrderRepository.search(command).map { persistenceOutcomeAdapterMapper.map(it) }

}