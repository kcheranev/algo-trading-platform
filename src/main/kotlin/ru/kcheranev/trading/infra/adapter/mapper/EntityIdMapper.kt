package ru.kcheranev.trading.infra.adapter.mapper

import org.mapstruct.Mapper
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.entity.TradeOrderId
import ru.kcheranev.trading.domain.entity.TradeSessionId

@Mapper
abstract class EntityIdMapper {

    fun mapLongToTradeSessionId(source: Long): TradeSessionId = TradeSessionId(source)

    fun mapTradeSessionIdToLong(source: TradeSessionId?): Long? = source?.value

    fun mapLongToStrategyConfigurationId(source: Long): StrategyConfigurationId = StrategyConfigurationId(source)

    fun mapStrategyConfigurationIdToLong(source: StrategyConfigurationId?): Long? = source?.value

    fun mapLongToTradeOrderId(source: Long): TradeOrderId = TradeOrderId(source)

    fun mapTradeOrderIdToLong(source: TradeOrderId?): Long? = source?.value

}