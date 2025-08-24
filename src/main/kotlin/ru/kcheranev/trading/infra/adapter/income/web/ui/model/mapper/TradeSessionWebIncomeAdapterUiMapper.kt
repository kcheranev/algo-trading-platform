package ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.income.tradesession.CreateTradeSessionCommand
import ru.kcheranev.trading.core.port.income.tradesession.SearchTradeSessionCommand
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.model.view.TradeSessionView
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.CreateTradeSessionRequestUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.SearchTradeSessionRequestUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.response.TradeSessionUiDto
import ru.kcheranev.trading.infra.adapter.mapper.EntityIdMapper

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