package ru.kcheranev.trading.infra.adapter.outcome.persistence

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.TradeOrder
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.model.TradeStrategy
import ru.kcheranev.trading.infra.adapter.mapper.EntityIdMapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeOrderEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity

@Mapper(uses = [EntityIdMapper::class])
interface PersistenceOutcomeAdapterMapper {

    fun map(source: TradeSession): TradeSessionEntity

    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.ticker", target = "ticker")
    @Mapping(source = "entity.instrumentId", target = "instrumentId")
    @Mapping(source = "entity.status", target = "status")
    @Mapping(source = "entity.startDate", target = "startDate")
    @Mapping(source = "entity.candleInterval", target = "candleInterval")
    @Mapping(source = "entity.lotsQuantity", target = "lotsQuantity")
    @Mapping(source = "entity.lastEventDate", target = "lastEventDate")
    @Mapping(source = "tradeStrategy", target = "strategy")
    @Mapping(source = "entity.strategyConfigurationId", target = "strategyConfigurationId")
    fun map(entity: TradeSessionEntity, tradeStrategy: TradeStrategy): TradeSession

    fun map(source: TradeOrder): TradeOrderEntity

    fun map(source: TradeOrderEntity): TradeOrder

    fun map(source: StrategyConfiguration): StrategyConfigurationEntity

    fun map(source: StrategyConfigurationEntity): StrategyConfiguration

}

val persistenceOutcomeAdapterMapper: PersistenceOutcomeAdapterMapper =
    Mappers.getMapper(PersistenceOutcomeAdapterMapper::class.java)