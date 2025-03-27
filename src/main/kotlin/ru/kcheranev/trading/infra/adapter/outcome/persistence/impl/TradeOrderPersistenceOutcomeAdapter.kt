package ru.kcheranev.trading.infra.adapter.outcome.persistence.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation.MANDATORY
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.port.outcome.persistence.tradeorder.GetTradeOrderCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradeorder.InsertTradeOrderCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradeorder.SearchTradeOrderCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradeorder.TradeOrderPersistencePort
import ru.kcheranev.trading.infra.adapter.outcome.persistence.PersistenceNotFoundException
import ru.kcheranev.trading.infra.adapter.outcome.persistence.persistenceOutcomeAdapterMapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.TradeOrderRepository

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