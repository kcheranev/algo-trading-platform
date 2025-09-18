package com.github.trading.infra.adapter.outcome.persistence.impl

import com.github.trading.core.port.outcome.persistence.tradeorder.GetTradeOrderCommand
import com.github.trading.core.port.outcome.persistence.tradeorder.InsertTradeOrderCommand
import com.github.trading.core.port.outcome.persistence.tradeorder.SearchTradeOrderCommand
import com.github.trading.core.port.outcome.persistence.tradeorder.TradeOrderPersistencePort
import com.github.trading.infra.adapter.outcome.persistence.PersistenceNotFoundException
import com.github.trading.infra.adapter.outcome.persistence.persistenceOutcomeAdapterMapper
import com.github.trading.infra.adapter.outcome.persistence.repository.TradeOrderRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation.MANDATORY
import org.springframework.transaction.annotation.Transactional

@Component
class TradeOrderPersistenceOutcomeAdapter(
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val tradeOrderRepository: TradeOrderRepository,
    private val eventPublisher: ApplicationEventPublisher
) : TradeOrderPersistencePort {

    @Transactional(propagation = MANDATORY)
    override fun insert(command: InsertTradeOrderCommand) {
        val tradeOrder = command.tradeOrder
        jdbcTemplate.insert(persistenceOutcomeAdapterMapper.map(tradeOrder))
        tradeOrder.events.forEach { eventPublisher.publishEvent(it) }
        tradeOrder.clearEvents()
    }

    override fun get(command: GetTradeOrderCommand) =
        tradeOrderRepository.findById(command.tradeOrderId.value)
            .orElseThrow {
                PersistenceNotFoundException("Trade order entity with id ${command.tradeOrderId.value} is not exists")
            }
            .let(persistenceOutcomeAdapterMapper::map)


    override fun search(command: SearchTradeOrderCommand) =
        tradeOrderRepository.search(command).map(persistenceOutcomeAdapterMapper::map)

}