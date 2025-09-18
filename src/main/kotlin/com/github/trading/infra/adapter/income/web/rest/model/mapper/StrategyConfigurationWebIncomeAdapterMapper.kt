package com.github.trading.infra.adapter.income.web.rest.model.mapper

import com.github.trading.core.port.income.strategyconfiguration.CreateStrategyConfigurationCommand
import com.github.trading.core.port.income.strategyconfiguration.SearchStrategyConfigurationCommand
import com.github.trading.domain.entity.StrategyConfiguration
import com.github.trading.infra.adapter.income.web.rest.model.request.CreateStrategyConfigurationRequestDto
import com.github.trading.infra.adapter.income.web.rest.model.request.SearchStrategyConfigurationRequestDto
import com.github.trading.infra.adapter.income.web.rest.model.response.StrategyConfigurationDto
import com.github.trading.infra.adapter.mapper.EntityIdMapper
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

@Mapper(uses = [EntityIdMapper::class])
interface StrategyConfigurationWebIncomeAdapterMapper {

    fun map(source: CreateStrategyConfigurationRequestDto): CreateStrategyConfigurationCommand

    fun map(source: SearchStrategyConfigurationRequestDto): SearchStrategyConfigurationCommand

    fun map(source: StrategyConfiguration): StrategyConfigurationDto

}

val strategyConfigurationWebIncomeAdapterMapper: StrategyConfigurationWebIncomeAdapterMapper = Mappers.getMapper(
    StrategyConfigurationWebIncomeAdapterMapper::class.java
)