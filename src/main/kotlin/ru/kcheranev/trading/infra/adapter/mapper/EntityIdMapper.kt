package ru.kcheranev.trading.infra.adapter.mapper

import org.mapstruct.Mapper
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.entity.TradeOrderId
import ru.kcheranev.trading.domain.entity.TradeSessionId
import java.util.UUID

@Mapper
abstract class EntityIdMapper {

    fun mapUuidToTradeSessionId(source: UUID?) = source?.let { TradeSessionId(it) }

    fun mapTradeSessionIdToUuid(source: TradeSessionId?) = source?.value

    fun mapUuidToStrategyConfigurationId(source: UUID?) = source?.let { StrategyConfigurationId(it) }

    fun mapStrategyConfigurationIdToUuid(source: StrategyConfigurationId?) = source?.value

    fun mapUuidToTradeOrderId(source: UUID?) = source?.let { TradeOrderId(it) }

    fun mapTradeOrderIdToUuid(source: TradeOrderId?) = source?.value

}