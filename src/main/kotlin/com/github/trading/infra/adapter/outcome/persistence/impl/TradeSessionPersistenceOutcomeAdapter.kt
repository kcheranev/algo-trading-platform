package com.github.trading.infra.adapter.outcome.persistence.impl

import com.github.trading.core.port.outcome.persistence.tradesession.GetReadyToOrderTradeSessionsCommand
import com.github.trading.core.port.outcome.persistence.tradesession.GetTradeSessionCommand
import com.github.trading.core.port.outcome.persistence.tradesession.InsertTradeSessionCommand
import com.github.trading.core.port.outcome.persistence.tradesession.IsReadyToOrderTradeSessionExistsCommand
import com.github.trading.core.port.outcome.persistence.tradesession.SaveTradeSessionCommand
import com.github.trading.core.port.outcome.persistence.tradesession.SearchTradeSessionCommand
import com.github.trading.core.port.outcome.persistence.tradesession.TradeSessionPersistencePort
import com.github.trading.core.port.service.TradeStrategyServicePort
import com.github.trading.core.port.service.command.InitTradeStrategyCommand
import com.github.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyProvider
import com.github.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyType
import com.github.trading.domain.entity.TradeSession
import com.github.trading.domain.entity.TradeSessionStatus
import com.github.trading.domain.model.Instrument
import com.github.trading.domain.model.StrategyParameters
import com.github.trading.domain.model.TradeStrategy
import com.github.trading.infra.adapter.outcome.persistence.PersistenceNotFoundException
import com.github.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import com.github.trading.infra.adapter.outcome.persistence.persistenceOutcomeAdapterMapper
import com.github.trading.infra.adapter.outcome.persistence.repository.TradeSessionRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation.MANDATORY
import org.springframework.transaction.annotation.Transactional

@Component
class TradeSessionPersistenceOutcomeAdapter(
    private val jdbcTemplate: JdbcAggregateTemplate,
    private val tradeSessionRepository: TradeSessionRepository,
    private val tradeStrategyCache: TradeStrategyCache,
    private val tradeStrategyServicePort: TradeStrategyServicePort,
    private val orderLotsQuantityStrategyProvider: OrderLotsQuantityStrategyProvider,
    private val eventPublisher: ApplicationEventPublisher
) : TradeSessionPersistencePort {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(propagation = MANDATORY)
    override fun insert(command: InsertTradeSessionCommand) {
        val tradeSession = command.tradeSession
        jdbcTemplate.insert(persistenceOutcomeAdapterMapper.map(tradeSession))
        tradeSession.events.forEach { eventPublisher.publishEvent(it) }
        tradeSession.clearEvents()
        tradeStrategyCache.put(command.tradeSession.id.value, tradeSession.strategy)
    }

    @Transactional(propagation = MANDATORY)
    override fun save(command: SaveTradeSessionCommand) {
        val tradeSession = command.tradeSession
        tradeSessionRepository.save(persistenceOutcomeAdapterMapper.map(tradeSession))
        tradeSession.events.forEach { eventPublisher.publishEvent(it) }
        tradeSession.clearEvents()
        if (tradeSession.status == TradeSessionStatus.STOPPED) {
            tradeStrategyCache.remove(command.tradeSession.id.value)
        }
    }

    private fun getOrderLotsQuantityStrategy(orderLotsQuantityStrategyType: OrderLotsQuantityStrategyType) =
        orderLotsQuantityStrategyProvider.getOrderLotsQuantityStrategy(orderLotsQuantityStrategyType)

    override fun get(command: GetTradeSessionCommand): TradeSession {
        val tradeSessionId = command.tradeSessionId
        val tradeSessionEntity =
            tradeSessionRepository.findById(tradeSessionId.value)
                .orElseThrow {
                    PersistenceNotFoundException("Trade session entity with id ${tradeSessionId.value} is not exist")
                }
        return persistenceOutcomeAdapterMapper.map(
            tradeSessionEntity,
            getOrCreateTradeStrategy(tradeSessionEntity),
            getOrderLotsQuantityStrategy(tradeSessionEntity.orderLotsQuantityStrategyType)
        )
    }

    override fun search(command: SearchTradeSessionCommand) =
        tradeSessionRepository.search(command)
            .map(persistenceOutcomeAdapterMapper::map)

    override fun getReadyForOrderTradeSessions() =
        tradeSessionRepository.getReadyForOrderTradeSessions()
            .map { tradeSessionEntity ->
                persistenceOutcomeAdapterMapper.map(
                    tradeSessionEntity,
                    getOrCreateTradeStrategy(tradeSessionEntity),
                    getOrderLotsQuantityStrategy(tradeSessionEntity.orderLotsQuantityStrategyType)
                )
            }

    override fun getReadyForOrderTradeSessions(command: GetReadyToOrderTradeSessionsCommand) =
        tradeSessionRepository.getReadyForOrderTradeSessions(command.instrumentId, command.candleInterval)
            .map { tradeSessionEntity ->
                persistenceOutcomeAdapterMapper.map(
                    tradeSessionEntity,
                    getOrCreateTradeStrategy(tradeSessionEntity),
                    getOrderLotsQuantityStrategy(tradeSessionEntity.orderLotsQuantityStrategyType)
                )
            }

    override fun isReadyForOrderTradeSessionExists(command: IsReadyToOrderTradeSessionExistsCommand) =
        tradeSessionRepository.isReadyForOrderTradeSessionExists(command.instrumentId, command.candleInterval)

    private fun getOrCreateTradeStrategy(tradeSessionEntity: TradeSessionEntity): TradeStrategy =
        tradeStrategyCache.get(tradeSessionEntity.id)
            ?.takeIf { tradeStrategy -> tradeStrategy.isFreshCandleSeries(tradeSessionEntity.candleInterval) }
            ?: run {
                log.info(
                    "Candle series for the subscription ticker=${tradeSessionEntity.ticker}, " +
                            "candleInterval=${tradeSessionEntity.candleInterval} has been expired. Reinitializing..."
                )
                val tradeStrategy =
                    tradeStrategyServicePort.initTradeStrategy(
                        InitTradeStrategyCommand(
                            strategyType = tradeSessionEntity.strategyType,
                            instrument = Instrument(tradeSessionEntity.instrumentId, tradeSessionEntity.ticker),
                            candleInterval = tradeSessionEntity.candleInterval,
                            strategyParameters = StrategyParameters(tradeSessionEntity.strategyParameters.value)
                        )
                    )
                tradeStrategyCache.put(tradeSessionEntity.id, tradeStrategy)
                return@run tradeStrategy
            }

}