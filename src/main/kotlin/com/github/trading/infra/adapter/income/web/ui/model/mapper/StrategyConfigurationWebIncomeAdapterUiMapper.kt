package com.github.trading.infra.adapter.income.web.ui.model.mapper

import com.github.trading.core.port.income.strategyconfiguration.CreateStrategyConfigurationCommand
import com.github.trading.core.port.income.strategyconfiguration.SearchStrategyConfigurationCommand
import com.github.trading.domain.entity.StrategyConfiguration
import com.github.trading.infra.adapter.income.web.ui.model.request.CreateStrategyConfigurationRequestUiDto
import com.github.trading.infra.adapter.income.web.ui.model.request.SearchStrategyConfigurationRequestUiDto
import com.github.trading.infra.adapter.income.web.ui.model.response.StrategyConfigurationUiDto
import com.github.trading.infra.adapter.mapper.EntityIdMapper
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

@Mapper(uses = [EntityIdMapper::class])
interface StrategyConfigurationWebIncomeAdapterUiMapper {

    fun map(source: CreateStrategyConfigurationRequestUiDto): CreateStrategyConfigurationCommand

    fun map(source: SearchStrategyConfigurationRequestUiDto): SearchStrategyConfigurationCommand

    fun map(source: StrategyConfiguration): StrategyConfigurationUiDto

}

val strategyConfigurationWebIncomeAdapterUiMapper: StrategyConfigurationWebIncomeAdapterUiMapper = Mappers.getMapper(
    StrategyConfigurationWebIncomeAdapterUiMapper::class.java
)