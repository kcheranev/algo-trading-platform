package ru.kcheranev.trading.infra.adapter.income.web.rest.model.mapper

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.income.strategyconfiguration.CreateStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.income.strategyconfiguration.SearchStrategyConfigurationCommand
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.request.CreateStrategyConfigurationRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.request.SearchStrategyConfigurationRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.response.StrategyConfigurationDto
import ru.kcheranev.trading.infra.adapter.mapper.EntityIdMapper

@Mapper(uses = [EntityIdMapper::class])
interface StrategyConfigurationWebIncomeAdapterMapper {

    fun map(source: CreateStrategyConfigurationRequestDto): CreateStrategyConfigurationCommand

    fun map(source: SearchStrategyConfigurationRequestDto): SearchStrategyConfigurationCommand

    fun map(source: StrategyConfiguration): StrategyConfigurationDto

}

val strategyConfigurationWebIncomeAdapterMapper: StrategyConfigurationWebIncomeAdapterMapper = Mappers.getMapper(
    StrategyConfigurationWebIncomeAdapterMapper::class.java
)