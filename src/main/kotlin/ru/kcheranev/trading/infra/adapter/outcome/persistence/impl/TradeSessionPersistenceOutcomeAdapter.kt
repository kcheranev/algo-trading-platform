package ru.kcheranev.trading.infra.adapter.outcome.persistence.impl

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation.MANDATORY
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.port.outcome.persistence.GetTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.SaveTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.TradeSessionPersistencePort
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.infra.adapter.outcome.cache.impl.TradeStrategyCacheOutcomeAdapter
import ru.kcheranev.trading.infra.adapter.outcome.persistence.TradeSessionEntityNotExistsException
import ru.kcheranev.trading.infra.adapter.outcome.persistence.TradeStrategyCacheNotExistsException
import ru.kcheranev.trading.infra.adapter.outcome.persistence.persistenceOutcomeAdapterMapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.TradeSessionRepository

@Component
class TradeSessionPersistenceOutcomeAdapter(
    private val tradeSessionRepository: TradeSessionRepository,
    private val tradeSessionCacheOutcomeAdapter: TradeStrategyCacheOutcomeAdapter
) : TradeSessionPersistencePort {

    @Transactional(propagation = MANDATORY)
    override fun save(command: SaveTradeSessionCommand): TradeSessionId {
        val savedTradeSessionEntity =
            tradeSessionRepository.save(persistenceOutcomeAdapterMapper.map(command.tradeSession))
        val tradeSessionId = TradeSessionId(savedTradeSessionEntity.id!!)
        tradeSessionCacheOutcomeAdapter.put(tradeSessionId, command.tradeSession.strategy)
        return tradeSessionId
    }

    override fun get(command: GetTradeSessionCommand): TradeSession {
        val tradeSessionId = command.tradeSessionId
        val tradeSessionEntity =
            tradeSessionRepository.findById(tradeSessionId.value)
                .orElseThrow { TradeSessionEntityNotExistsException(tradeSessionId) }
        val tradeStrategy =
            tradeSessionCacheOutcomeAdapter.get(tradeSessionId)
                ?: throw TradeStrategyCacheNotExistsException(tradeSessionId)
        return persistenceOutcomeAdapterMapper.map(tradeSessionEntity, tradeStrategy)
    }

}