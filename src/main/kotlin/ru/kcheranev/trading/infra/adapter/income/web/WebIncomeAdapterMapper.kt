package ru.kcheranev.trading.infra.adapter.income.web

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAdjustAndAnalyzeCommand
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeCommand
import ru.kcheranev.trading.core.port.income.search.StrategyConfigurationSearchCommand
import ru.kcheranev.trading.core.port.income.search.TradeOrderSearchCommand
import ru.kcheranev.trading.core.port.income.search.TradeSessionSearchCommand
import ru.kcheranev.trading.core.port.income.trading.CreateStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.income.trading.StartTradeSessionCommand
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.TradeOrder
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.model.backtesting.StrategyAdjustAndAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.StrategyAnalyzeResult
import ru.kcheranev.trading.infra.adapter.income.web.model.request.CreateStrategyConfigurationRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StartTradeSessionRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StrategyAdjustAndAnalyzeRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StrategyAnalyzeRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StrategyConfigurationSearchRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.model.request.TradeOrderSearchRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.model.request.TradeSessionSearchRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.model.response.StrategyAdjustAndAnalyzeDto
import ru.kcheranev.trading.infra.adapter.income.web.model.response.StrategyAnalyzeDto
import ru.kcheranev.trading.infra.adapter.income.web.model.response.StrategyConfigurationDto
import ru.kcheranev.trading.infra.adapter.income.web.model.response.TradeOrderDto
import ru.kcheranev.trading.infra.adapter.income.web.model.response.TradeSessionDto
import ru.kcheranev.trading.infra.adapter.mapper.EntityIdMapper

@Mapper(uses = [EntityIdMapper::class])
interface WebIncomeAdapterMapper {

    fun map(source: CreateStrategyConfigurationRequestDto): CreateStrategyConfigurationCommand

    fun map(source: StartTradeSessionRequestDto): StartTradeSessionCommand

    fun map(source: TradeSessionSearchRequestDto): TradeSessionSearchCommand

    fun map(source: StrategyConfigurationSearchRequestDto): StrategyConfigurationSearchCommand

    fun map(source: TradeOrderSearchRequestDto): TradeOrderSearchCommand

    fun map(source: StrategyAnalyzeRequestDto): StrategyAnalyzeCommand

    fun map(source: StrategyAdjustAndAnalyzeRequestDto): StrategyAdjustAndAnalyzeCommand

    fun map(source: TradeSession): TradeSessionDto

    fun map(source: StrategyConfiguration): StrategyConfigurationDto

    fun map(source: TradeOrder): TradeOrderDto

    fun map(source: StrategyAnalyzeResult): StrategyAnalyzeDto

    fun map(source: StrategyAdjustAndAnalyzeResult): StrategyAdjustAndAnalyzeDto

}

val webIncomeAdapterMapper: WebIncomeAdapterMapper = Mappers.getMapper(WebIncomeAdapterMapper::class.java)