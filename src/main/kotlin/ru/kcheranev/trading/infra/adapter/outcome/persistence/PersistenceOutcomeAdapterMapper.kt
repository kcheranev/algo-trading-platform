package ru.kcheranev.trading.infra.adapter.outcome.persistence

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.domain.entity.Instrument
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.TradeOrder
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.TradeStrategy
import ru.kcheranev.trading.domain.model.view.TradeSessionView
import ru.kcheranev.trading.infra.adapter.mapper.EntityIdMapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.InstrumentEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeOrderEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper

@Mapper(uses = [EntityIdMapper::class])
abstract class PersistenceOutcomeAdapterMapper {

    @Mapping(source = "currentPosition.lotsQuantity", target = "positionLotsQuantity")
    @Mapping(source = "currentPosition.averagePrice", target = "positionAveragePrice")
    abstract fun map(source: TradeSession): TradeSessionEntity

    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.ticker", target = "ticker")
    @Mapping(source = "entity.instrumentId", target = "instrumentId")
    @Mapping(source = "entity.status", target = "status")
    @Mapping(source = "entity.candleInterval", target = "candleInterval")
    @Mapping(source = "entity.lotsQuantity", target = "lotsQuantity")
    @Mapping(source = "entity.positionLotsQuantity", target = "currentPosition.lotsQuantity")
    @Mapping(source = "entity.positionAveragePrice", target = "currentPosition.averagePrice")
    @Mapping(source = "tradeStrategy", target = "strategy")
    @Mapping(target = "events", ignore = true)
    abstract fun map(entity: TradeSessionEntity, tradeStrategy: TradeStrategy): TradeSession

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

    fun map(source: MapWrapper<String>) = StrategyParameters(source.value)

}

val persistenceOutcomeAdapterMapper: PersistenceOutcomeAdapterMapper =
    Mappers.getMapper(PersistenceOutcomeAdapterMapper::class.java)