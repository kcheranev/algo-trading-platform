package ru.kcheranev.trading.infra.adapter.income.web

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.income.search.StrategyConfigurationSearchCommand
import ru.kcheranev.trading.core.port.income.search.TradeOrderSearchCommand
import ru.kcheranev.trading.core.port.income.search.TradeSessionSearchCommand
import ru.kcheranev.trading.core.port.income.trading.CreateStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.income.trading.StartTradeSessionCommand
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.TradeOrder
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.infra.adapter.income.web.model.request.CreateStrategyConfigurationRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StartTradeSessionRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StrategyConfigurationSearchRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.request.TradeOrderSearchRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.request.TradeSessionSearchRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.response.StrategyConfigurationDto
import ru.kcheranev.trading.infra.adapter.income.web.model.response.TradeOrderDto
import ru.kcheranev.trading.infra.adapter.income.web.model.response.TradeSessionDto
import ru.kcheranev.trading.infra.adapter.mapper.EntityIdMapper

@Mapper(uses = [EntityIdMapper::class])
interface WebIncomeAdapterMapper {

    fun map(source: CreateStrategyConfigurationRequest): CreateStrategyConfigurationCommand

    fun map(source: StartTradeSessionRequest): StartTradeSessionCommand

    fun map(source: TradeSessionSearchRequest): TradeSessionSearchCommand

    fun map(source: StrategyConfigurationSearchRequest): StrategyConfigurationSearchCommand

    fun map(source: TradeOrderSearchRequest): TradeOrderSearchCommand

    fun map(source: TradeSession): TradeSessionDto

    fun map(source: StrategyConfiguration): StrategyConfigurationDto

    fun map(source: TradeOrder): TradeOrderDto

}

val webIncomeAdapterMapper: WebIncomeAdapterMapper = Mappers.getMapper(WebIncomeAdapterMapper::class.java)