package ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper

import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers
import ru.kcheranev.trading.core.port.income.strategyconfiguration.CreateStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.income.strategyconfiguration.SearchStrategyConfigurationCommand
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.CreateStrategyConfigurationRequestUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.SearchStrategyConfigurationRequestUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.response.StrategyConfigurationUiDto
import ru.kcheranev.trading.infra.adapter.mapper.EntityIdMapper

@Mapper(uses = [EntityIdMapper::class])
interface StrategyConfigurationWebIncomeAdapterUiMapper {

    fun map(source: CreateStrategyConfigurationRequestUiDto): CreateStrategyConfigurationCommand

    fun map(source: SearchStrategyConfigurationRequestUiDto): SearchStrategyConfigurationCommand

    fun map(source: StrategyConfiguration): StrategyConfigurationUiDto

}

val strategyConfigurationWebIncomeAdapterUiMapper: StrategyConfigurationWebIncomeAdapterUiMapper = Mappers.getMapper(
    StrategyConfigurationWebIncomeAdapterUiMapper::class.java
)