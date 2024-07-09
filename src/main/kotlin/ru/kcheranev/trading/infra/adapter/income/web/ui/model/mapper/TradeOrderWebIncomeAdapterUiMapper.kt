package ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.income.tradeorder.SearchTradeOrderCommand
import ru.kcheranev.trading.domain.entity.TradeOrder
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.SearchTradeOrderRequestUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.response.TradeOrderUiDto
import ru.kcheranev.trading.infra.adapter.mapper.EntityIdMapper

@Mapper(uses = [EntityIdMapper::class])
interface TradeOrderWebIncomeAdapterUiMapper {

    fun map(source: SearchTradeOrderRequestUiDto): SearchTradeOrderCommand

    fun map(source: TradeOrder): TradeOrderUiDto

}

val tradeOrderWebIncomeAdapterUiMapper: TradeOrderWebIncomeAdapterUiMapper = Mappers.getMapper(
    TradeOrderWebIncomeAdapterUiMapper::class.java
)