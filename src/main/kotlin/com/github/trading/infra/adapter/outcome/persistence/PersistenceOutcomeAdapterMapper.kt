package com.github.trading.infra.adapter.outcome.persistence

import com.github.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategy
import com.github.trading.domain.entity.Instrument
import com.github.trading.domain.entity.StrategyConfiguration
import com.github.trading.domain.entity.TradeOrder
import com.github.trading.domain.entity.TradeSession
import com.github.trading.domain.model.StrategyParameters
import com.github.trading.domain.model.TradeStrategy
import com.github.trading.domain.model.view.TradeSessionView
import com.github.trading.infra.adapter.mapper.EntityIdMapper
import com.github.trading.infra.adapter.outcome.persistence.entity.InstrumentEntity
import com.github.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import com.github.trading.infra.adapter.outcome.persistence.entity.TradeOrderEntity
import com.github.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import com.github.trading.infra.adapter.outcome.persistence.model.MapWrapper
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.factory.Mappers

@Mapper(uses = [EntityIdMapper::class])
abstract class PersistenceOutcomeAdapterMapper {

    @Mapping(source = "currentPosition.lotsQuantity", target = "positionLotsQuantity")
    @Mapping(source = "currentPosition.averagePrice", target = "positionAveragePrice")
    @Mapping(source = "orderLotsQuantityStrategy.type", target = "orderLotsQuantityStrategyType")
    abstract fun map(source: TradeSession): TradeSessionEntity

    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.ticker", target = "ticker")
    @Mapping(source = "entity.instrumentId", target = "instrumentId")
    @Mapping(source = "entity.status", target = "status")
    @Mapping(source = "entity.candleInterval", target = "candleInterval")
    @Mapping(source = "entity.positionLotsQuantity", target = "currentPosition.lotsQuantity")
    @Mapping(source = "entity.positionAveragePrice", target = "currentPosition.averagePrice")
    @Mapping(source = "tradeStrategy", target = "strategy")
    @Mapping(source = "orderLotsQuantityStrategy", target = "orderLotsQuantityStrategy")
    @Mapping(target = "events", ignore = true)
    abstract fun map(
        entity: TradeSessionEntity,
        tradeStrategy: TradeStrategy,
        orderLotsQuantityStrategy: OrderLotsQuantityStrategy
    ): TradeSession

    @Mapping(source = "positionLotsQuantity", target = "currentPosition.lotsQuantity")
    @Mapping(source = "positionAveragePrice", target = "currentPosition.averagePrice")
    abstract fun map(source: TradeSessionEntity): TradeSessionView

    abstract fun map(source: TradeOrder): TradeOrderEntity

    @Mapping(target = "events", ignore = true)
    abstract fun map(source: TradeOrderEntity): TradeOrder

    abstract fun map(source: StrategyConfiguration): StrategyConfigurationEntity

    @Mapping(target = "events", ignore = true)
    abstract fun map(source: StrategyConfigurationEntity): StrategyConfiguration

    abstract fun map(source: Instrument): InstrumentEntity

    abstract fun map(source: InstrumentEntity): Instrument

    fun map(source: StrategyParameters) = MapWrapper(source)

    fun map(source: MapWrapper) = StrategyParameters(source.value)

}

val persistenceOutcomeAdapterMapper: PersistenceOutcomeAdapterMapper =
    Mappers.getMapper(PersistenceOutcomeAdapterMapper::class.java)