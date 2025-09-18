package com.github.trading.infra.adapter.income.web.ui.model.mapper

import com.github.trading.core.port.income.tradesession.CreateTradeSessionCommand
import com.github.trading.core.port.income.tradesession.SearchTradeSessionCommand
import com.github.trading.domain.entity.TradeSession
import com.github.trading.domain.model.view.TradeSessionView
import com.github.trading.infra.adapter.income.web.ui.model.request.CreateTradeSessionRequestUiDto
import com.github.trading.infra.adapter.income.web.ui.model.request.SearchTradeSessionRequestUiDto
import com.github.trading.infra.adapter.income.web.ui.model.response.TradeSessionUiDto
import com.github.trading.infra.adapter.mapper.EntityIdMapper
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.factory.Mappers

@Mapper(uses = [EntityIdMapper::class])
interface TradeSessionWebIncomeAdapterUiMapper {

    fun map(source: CreateTradeSessionRequestUiDto): CreateTradeSessionCommand

    fun map(source: SearchTradeSessionRequestUiDto): SearchTradeSessionCommand

    @Mapping(source = "orderLotsQuantityStrategy.type", target = "orderLotsQuantityStrategyType")
    fun map(source: TradeSession): TradeSessionUiDto

    fun map(source: TradeSessionView): TradeSessionUiDto

}

val tradeSessionWebIncomeAdapterUiMapper: TradeSessionWebIncomeAdapterUiMapper = Mappers.getMapper(
    TradeSessionWebIncomeAdapterUiMapper::class.java
)