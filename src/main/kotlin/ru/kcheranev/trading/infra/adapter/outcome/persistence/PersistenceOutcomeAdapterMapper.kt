package ru.kcheranev.trading.infra.adapter.outcome.persistence

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.TradeOrder
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.infra.adapter.mapper.EntityIdMapper
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeOrderEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import java.util.UUID

@Mapper(uses = [EntityIdMapper::class])
abstract class PersistenceOutcomeAdapterMapper {

    @Mapping(source = "id", target = "id")
    abstract fun map(source: TradeSession, id: UUID): TradeSession

    abstract fun map(source: TradeOrder): TradeOrderEntity

    @Mapping(target = "events", ignore = true)
    abstract fun map(source: TradeOrderEntity): TradeOrder

    abstract fun map(source: StrategyConfiguration): StrategyConfigurationEntity

    @Mapping(target = "events", ignore = true)
    abstract fun map(source: StrategyConfigurationEntity): StrategyConfiguration

    fun map(source: StrategyParameters) = MapWrapper(source)

    fun map(source: MapWrapper<String, Int>) = StrategyParameters(source.value)

}

val persistenceOutcomeAdapterMapper: PersistenceOutcomeAdapterMapper =
    Mappers.getMapper(PersistenceOutcomeAdapterMapper::class.java)