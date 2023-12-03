package ru.kcheranev.trading.infra.adapter.outcome.persistence.impl

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation.MANDATORY
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.port.outcome.persistence.GetReadyToOrderTradeSessionsCommand
import ru.kcheranev.trading.core.port.outcome.persistence.GetTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.SaveTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.TradeSessionPersistencePort
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.model.TradeStrategy
import ru.kcheranev.trading.infra.adapter.outcome.persistence.TradeSessionEntityNotExistsException
import ru.kcheranev.trading.infra.adapter.outcome.persistence.TradeStrategyCacheNotExistsException
import ru.kcheranev.trading.infra.adapter.outcome.persistence.persistenceOutcomeAdapterMapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.TradeSessionRepository
import java.util.concurrent.ConcurrentHashMap

@Component
class TradeSessionPersistenceOutcomeAdapter(
    private val tradeSessionRepository: TradeSessionRepository
) : TradeSessionPersistencePort {

    private val tradeStrategies = ConcurrentHashMap<Long, TradeStrategy>()

    private fun getTradeStrategy(tradeSessionId: Long): TradeStrategy =
        tradeStrategies[tradeSessionId] ?: throw TradeStrategyCacheNotExistsException(tradeSessionId)

    @Transactional(propagation = MANDATORY)
    override fun save(command: SaveTradeSessionCommand): TradeSessionId {
        val savedTradeSessionEntity =
            tradeSessionRepository.save(persistenceOutcomeAdapterMapper.map(command.tradeSession))
        val tradeSessionId = savedTradeSessionEntity.id!!
        tradeStrategies[tradeSessionId] = command.tradeSession.strategy
        return TradeSessionId(tradeSessionId)
    }

    override fun get(command: GetTradeSessionCommand): TradeSession {
        val tradeSessionId = command.tradeSessionId
        val tradeSessionEntity =
            tradeSessionRepository.findById(tradeSessionId.value)
                .orElseThrow { TradeSessionEntityNotExistsException(tradeSessionId) }
        val tradeStrategy = getTradeStrategy(tradeSessionId.value)
        return persistenceOutcomeAdapterMapper.map(tradeSessionEntity, tradeStrategy)
    }

    override fun getReadyToOrderTradeSessions(command: GetReadyToOrderTradeSessionsCommand): List<TradeSession> =
        tradeSessionRepository.getReadyToOrderTradeSessions(command.instrumentId, command.candleInterval)
            .filter { tradeStrategies.contains(it.id) }
            .map {
                persistenceOutcomeAdapterMapper.map(
                    it,
                    getTradeStrategy(it.id!!)
                )
            }

}