package ru.kcheranev.trading.infra.adapter.income.web.rest.model.mapper

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.income.tradesession.SearchTradeSessionCommand
import ru.kcheranev.trading.core.port.income.tradesession.StartTradeSessionCommand
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.model.view.TradeSessionView
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.request.SearchTradeSessionRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.request.StartTradeSessionRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.response.TradeSessionDto
import ru.kcheranev.trading.infra.adapter.mapper.EntityIdMapper

@Mapper(uses = [EntityIdMapper::class])
interface TradeSessionWebIncomeAdapterMapper {

    fun map(source: StartTradeSessionRequestDto): StartTradeSessionCommand

    fun map(source: SearchTradeSessionRequestDto): SearchTradeSessionCommand

    fun map(source: TradeSession): TradeSessionDto

    fun map(source: TradeSessionView): TradeSessionDto

}

val tradeSessionWebIncomeAdapterMapper: TradeSessionWebIncomeAdapterMapper = Mappers.getMapper(
    TradeSessionWebIncomeAdapterMapper::class.java
)