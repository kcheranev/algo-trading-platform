package ru.kcheranev.trading.infra.adapter.outcome.persistence.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation.MANDATORY
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.port.outcome.persistence.GetReadyToOrderTradeSessionsCommand
import ru.kcheranev.trading.core.port.outcome.persistence.GetTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.SaveTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.TradeSessionPersistencePort
import ru.kcheranev.trading.core.port.outcome.persistence.TradeSessionSearchCommand
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.infra.adapter.outcome.PersistenceOutcomeAdapterException
import ru.kcheranev.trading.infra.adapter.outcome.persistence.persistenceOutcomeAdapterMapper
import java.util.UUID

@Component
class TradeSessionPersistenceOutcomeAdapter(
    private val tradeSessionCache: TradeSessionCache,
    private val eventPublisher: ApplicationEventPublisher
) : TradeSessionPersistencePort {

    @Transactional(propagation = MANDATORY)
    override fun save(command: SaveTradeSessionCommand): TradeSessionId {
        val tradeSession =
            if (command.tradeSession.id != null) {
                command.tradeSession
            } else {
                command.tradeSession
                    .let {
                        persistenceOutcomeAdapterMapper.map(it, it.id?.value ?: UUID.randomUUID())
                    }
            }
        val tradeSessionId = tradeSession.id!!.value
        if (tradeSession.status.terminal) {
            tradeSessionCache.remove(tradeSessionId)
        } else {
            tradeSessionCache.put(tradeSessionId, tradeSession)
        }
        tradeSession.events.forEach { eventPublisher.publishEvent(it) }
        tradeSession.clearEvents()
        return TradeSessionId(tradeSessionId)
    }

    override fun get(command: GetTradeSessionCommand) =
        tradeSessionCache.get(command.tradeSessionId.value)
            ?: throw PersistenceOutcomeAdapterException("Trade session entity with id ${command.tradeSessionId.value} is not exists")

    override fun search(command: TradeSessionSearchCommand) =
        tradeSessionCache.search(command)

    override fun getReadyToOrderTradeSessions(command: GetReadyToOrderTradeSessionsCommand) =
        tradeSessionCache.findAll().filter { tradeSession -> tradeSession.readyForOrder() }

}