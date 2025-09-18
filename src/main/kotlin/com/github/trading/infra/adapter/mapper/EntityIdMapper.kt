package com.github.trading.infra.adapter.mapper

import com.github.trading.domain.entity.InstrumentId
import com.github.trading.domain.entity.StrategyConfigurationId
import com.github.trading.domain.entity.TradeOrderId
import com.github.trading.domain.entity.TradeSessionId
import org.mapstruct.Mapper
import java.util.UUID

@Mapper
abstract class EntityIdMapper {

    fun mapUuidToTradeSessionId(source: UUID?) = source?.let { TradeSessionId(it) }

    fun mapTradeSessionIdToUuid(source: TradeSessionId?) = source?.value

    fun mapUuidToStrategyConfigurationId(source: UUID?) = source?.let { StrategyConfigurationId(it) }

    fun mapStrategyConfigurationIdToUuid(source: StrategyConfigurationId?) = source?.value

    fun mapUuidToTradeOrderId(source: UUID?) = source?.let { TradeOrderId(it) }

    fun mapTradeOrderIdToUuid(source: TradeOrderId?) = source?.value

    fun mapUuidToInstrumentId(source: UUID?) = source?.let { InstrumentId(it) }

    fun mapInstrumentIdToUuid(source: InstrumentId?) = source?.value

}