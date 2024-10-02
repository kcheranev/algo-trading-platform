package ru.kcheranev.trading.infra.adapter.outcome.persistence.impl

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation.MANDATORY
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.GetReadyToOrderTradeSessionsCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.GetTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.InsertTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.SaveTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.SearchTradeSessionCommand
import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.TradeSessionPersistencePort
import ru.kcheranev.trading.core.port.service.TradeStrategyServicePort
import ru.kcheranev.trading.core.port.service.command.InitTradeStrategyCommand
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.infra.adapter.outcome.persistence.PersistenceNotFoundException
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.persistenceOutcomeAdapterMapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.TradeSessionRepository

@Component
class TradeSessionPersistenceOutcomeAdapter(
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val tradeSessionRepository: TradeSessionRepository,
    private val tradeStrategyCache: TradeStrategyCache,
    private val tradeStrategyServicePort: TradeStrategyServicePort,
    private val eventPublisher: ApplicationEventPublisher
) : TradeSessionPersistencePort {

    @Transactional(propagation = MANDATORY)
    override fun insert(command: InsertTradeSessionCommand): TradeSessionId {
        val tradeSession = command.tradeSession
        val savedTradeSessionEntity =
            jdbcTemplate.insert(persistenceOutcomeAdapterMapper.map(tradeSession))
        tradeSession.events.forEach { eventPublisher.publishEvent(it) }
        tradeSession.clearEvents()
        val tradeSessionId = savedTradeSessionEntity.id
        tradeStrategyCache.put(tradeSessionId, tradeSession.strategy)
        return TradeSessionId(tradeSessionId)
    }

    @Transactional(propagation = MANDATORY)
    override fun save(command: SaveTradeSessionCommand): TradeSessionId {
        val tradeSession = command.tradeSession
        val savedTradeSessionEntity =
            tradeSessionRepository.save(persistenceOutcomeAdapterMapper.map(tradeSession))
        tradeSession.events.forEach { eventPublisher.publishEvent(it) }
        tradeSession.clearEvents()
        val tradeSessionId = savedTradeSessionEntity.id
        if (tradeSession.isTerminal()) {
            tradeStrategyCache.remove(tradeSessionId)
        }
        return TradeSessionId(tradeSessionId)
    }

    override fun get(command: GetTradeSessionCommand): TradeSession {
        val tradeSessionId = command.tradeSessionId
        val tradeSessionEntity =
            tradeSessionRepository.findById(tradeSessionId.value)
                .orElseThrow {
                    PersistenceNotFoundException("Trade session entity with id ${tradeSessionId.value} is not exist")
                }
        val tradeStrategy = getOrCreateTradeStrategy(tradeSessionEntity)
        return persistenceOutcomeAdapterMapper.map(tradeSessionEntity, tradeStrategy)
    }

    override fun search(command: SearchTradeSessionCommand) =
        tradeSessionRepository.search(command)
            .map { persistenceOutcomeAdapterMapper.map(it) }

    override fun getReadyForOrderTradeSessions(command: GetReadyToOrderTradeSessionsCommand) =
        tradeSessionRepository.getReadyForOrderTradeSessions(command.instrumentId, command.candleInterval)
            .map { persistenceOutcomeAdapterMapper.map(it, getOrCreateTradeStrategy(it)) }

    private fun getOrCreateTradeStrategy(tradeSessionEntity: TradeSessionEntity) =
        tradeStrategyCache.computeIfAbsent(tradeSessionEntity.id) {
            tradeStrategyServicePort.initTradeStrategy(
                InitTradeStrategyCommand(
                    strategyType = tradeSessionEntity.strategyType,
                    instrument = Instrument(tradeSessionEntity.instrumentId, tradeSessionEntity.ticker),
                    candleInterval = tradeSessionEntity.candleInterval,
                    strategyParameters = StrategyParameters(tradeSessionEntity.strategyParameters.value)
                )
            )
        }

}