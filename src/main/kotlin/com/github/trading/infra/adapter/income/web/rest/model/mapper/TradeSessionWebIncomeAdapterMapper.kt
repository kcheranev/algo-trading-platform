package com.github.trading.infra.adapter.income.web.rest.model.mapper

import com.github.trading.core.port.income.tradesession.CreateTradeSessionCommand
import com.github.trading.core.port.income.tradesession.SearchTradeSessionCommand
import com.github.trading.domain.entity.TradeSession
import com.github.trading.domain.model.view.TradeSessionView
import com.github.trading.infra.adapter.income.web.rest.model.request.CreateTradeSessionRequestDto
import com.github.trading.infra.adapter.income.web.rest.model.request.SearchTradeSessionRequestDto
import com.github.trading.infra.adapter.income.web.rest.model.response.TradeSessionDto
import com.github.trading.infra.adapter.mapper.EntityIdMapper
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.factory.Mappers

@Mapper(uses = [EntityIdMapper::class])
interface TradeSessionWebIncomeAdapterMapper {

    fun map(source: CreateTradeSessionRequestDto): CreateTradeSessionCommand

    fun map(source: SearchTradeSessionRequestDto): SearchTradeSessionCommand

    @Mapping(source = "orderLotsQuantityStrategy.type", target = "orderLotsQuantityStrategyType")
    fun map(source: TradeSession): TradeSessionDto

    fun map(source: TradeSessionView): TradeSessionDto

}

val tradeSessionWebIncomeAdapterMapper: TradeSessionWebIncomeAdapterMapper = Mappers.getMapper(
    TradeSessionWebIncomeAdapterMapper::class.java
)