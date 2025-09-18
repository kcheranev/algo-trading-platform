package com.github.trading.infra.adapter.income.web.rest.model.mapper

import com.github.trading.core.port.income.tradeorder.SearchTradeOrderCommand
import com.github.trading.domain.entity.TradeOrder
import com.github.trading.infra.adapter.income.web.rest.model.request.SearchTradeOrderRequestDto
import com.github.trading.infra.adapter.income.web.rest.model.response.TradeOrderDto
import com.github.trading.infra.adapter.mapper.EntityIdMapper
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

@Mapper(uses = [EntityIdMapper::class])
interface TradeOrderWebIncomeAdapterMapper {

    fun map(source: SearchTradeOrderRequestDto): SearchTradeOrderCommand

    fun map(source: TradeOrder): TradeOrderDto

}

val tradeOrderWebIncomeAdapterMapper: TradeOrderWebIncomeAdapterMapper = Mappers.getMapper(
    TradeOrderWebIncomeAdapterMapper::class.java
)