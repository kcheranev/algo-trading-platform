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
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.infra.adapter.outcome.persistence.TradeSessionEntityNotExistsException
import ru.kcheranev.trading.infra.adapter.outcome.persistence.persistenceOutcomeAdapterMapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.TradeSessionRepository

@Component
class TradeSessionPersistenceOutcomeAdapter(
    private val tradeSessionRepository: TradeSessionRepository,
    private val tradeStrategyCache: TradeStrategyCache,
    private val eventPublisher: ApplicationEventPublisher
) : TradeSessionPersistencePort {

    @Transactional(propagation = MANDATORY)
    override fun save(command: SaveTradeSessionCommand): TradeSessionId {
        val tradeSession = command.tradeSession
        val savedTradeSessionEntity =
            tradeSessionRepository.save(persistenceOutcomeAdapterMapper.map(tradeSession))
        tradeSession.events.forEach { eventPublisher.publishEvent(it) }
        tradeSession.clearEvents()
        val tradeSessionId = savedTradeSessionEntity.id!!
        tradeStrategyCache.put(tradeSessionId, tradeSession.strategy)
        return TradeSessionId(tradeSessionId)
    }

    override fun get(command: GetTradeSessionCommand): TradeSession {
        val tradeSessionId = command.tradeSessionId
        val tradeSessionEntity =
            tradeSessionRepository.findById(tradeSessionId.value)
                .orElseThrow { TradeSessionEntityNotExistsException(tradeSessionId) }
        val tradeStrategy = tradeStrategyCache.get(tradeSessionId.value)
        return persistenceOutcomeAdapterMapper.map(tradeSessionEntity, tradeStrategy)
    }

    override fun search(command: TradeSessionSearchCommand): List<TradeSession> =
        tradeSessionRepository.search(command)
            .map { persistenceOutcomeAdapterMapper.map(it, tradeStrategyCache.get(it.id!!)) }

    override fun getReadyToOrderTradeSessions(command: GetReadyToOrderTradeSessionsCommand): List<TradeSession> =
        tradeSessionRepository.getReadyToOrderTradeSessions(command.instrumentId, command.candleInterval)
            .filter { tradeStrategyCache.contains(it.id!!) }
            .map {
                persistenceOutcomeAdapterMapper.map(
                    it,
                    tradeStrategyCache.get(it.id!!)
                )
            }

}