package ru.kcheranev.trading.infra.adapter.outcome.persistence.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation.MANDATORY
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.GetReadyToOrderTradeSessionsCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.GetTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.SaveTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.SearchTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.TradeSessionPersistencePort
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.infra.adapter.outcome.persistence.PersistenceNotFoundException
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
            ?: throw PersistenceNotFoundException("Trade session entity with id ${command.tradeSessionId.value} is not exists")

    override fun search(command: SearchTradeSessionCommand) =
        tradeSessionCache.search(command)

    override fun getReadyToOrderTradeSessions(command: GetReadyToOrderTradeSessionsCommand) =
        tradeSessionCache.findAll().filter { tradeSession -> tradeSession.readyForOrder() }

}