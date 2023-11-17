package ru.kcheranev.trading.infra.adapter.outcome.persistence

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.domain.entity.Order
import ru.kcheranev.trading.domain.entity.OrderId
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.model.TradeStrategy
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.OrderEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.StrategyConfigurationEntity
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity

@Mapper
interface PersistenceOutcomeAdapterMapper {

    fun mapTradeSessionId(source: Long): TradeSessionId {
        return TradeSessionId(source)
    }

    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "strategyConfigurationId.value", target = "strategyConfigurationId")
    fun map(source: TradeSession): TradeSessionEntity

    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.ticker", target = "ticker")
    @Mapping(source = "entity.status", target = "status")
    @Mapping(source = "entity.startDate", target = "startDate")
    @Mapping(source = "entity.candleInterval", target = "candleInterval")
    @Mapping(source = "entity.lastEventDate", target = "lastEventDate")
    @Mapping(source = "tradeStrategy", target = "strategy")
    @Mapping(source = "entity.strategyConfigurationId", target = "strategyConfigurationId")
    fun map(entity: TradeSessionEntity, tradeStrategy: TradeStrategy): TradeSession

    fun mapOrderId(source: Long): OrderId {
        return OrderId(source)
    }

    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "tradeSessionId.value", target = "tradeSessionId")
    fun map(source: Order): OrderEntity

    fun map(source: OrderEntity): Order

    fun mapStrategyConfigurationId(source: Long): StrategyConfigurationId {
        return StrategyConfigurationId(source)
    }

    @Mapping(source = "id.value", target = "id")
    fun map(source: StrategyConfiguration): StrategyConfigurationEntity

    fun map(source: StrategyConfigurationEntity): StrategyConfiguration

}

val persistenceOutcomeAdapterMapper: PersistenceOutcomeAdapterMapper =
    Mappers.getMapper(PersistenceOutcomeAdapterMapper::class.java)