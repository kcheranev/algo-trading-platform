package com.github.trading.infra.adapter.income.web.ui.model.mapper

import com.github.trading.core.port.income.tradeorder.SearchTradeOrderCommand
import com.github.trading.domain.entity.TradeOrder
import com.github.trading.infra.adapter.income.web.ui.model.request.SearchTradeOrderRequestUiDto
import com.github.trading.infra.adapter.income.web.ui.model.response.TradeOrderUiDto
import com.github.trading.infra.adapter.mapper.EntityIdMapper
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

@Mapper(uses = [EntityIdMapper::class])
interface TradeOrderWebIncomeAdapterUiMapper {

    fun map(source: SearchTradeOrderRequestUiDto): SearchTradeOrderCommand

    fun map(source: TradeOrder): TradeOrderUiDto

}

val tradeOrderWebIncomeAdapterUiMapper: TradeOrderWebIncomeAdapterUiMapper = Mappers.getMapper(
    TradeOrderWebIncomeAdapterUiMapper::class.java
)