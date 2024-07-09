package ru.kcheranev.trading.infra.adapter.income.web.rest.model.mapper

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.income.tradeorder.SearchTradeOrderCommand
import ru.kcheranev.trading.domain.entity.TradeOrder
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.request.SearchTradeOrderRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.response.TradeOrderDto
import ru.kcheranev.trading.infra.adapter.mapper.EntityIdMapper

@Mapper(uses = [EntityIdMapper::class])
interface TradeOrderWebIncomeAdapterMapper {

    fun map(source: SearchTradeOrderRequestDto): SearchTradeOrderCommand

    fun map(source: TradeOrder): TradeOrderDto

}

val tradeOrderWebIncomeAdapterMapper: TradeOrderWebIncomeAdapterMapper = Mappers.getMapper(
    TradeOrderWebIncomeAdapterMapper::class.java
)