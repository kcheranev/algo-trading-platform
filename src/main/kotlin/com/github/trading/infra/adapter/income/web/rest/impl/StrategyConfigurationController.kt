package com.github.trading.infra.adapter.income.web.rest.impl

import com.github.trading.core.port.income.strategyconfiguration.CreateStrategyConfigurationUseCase
import com.github.trading.core.port.income.strategyconfiguration.SearchStrategyConfigurationUseCase
import com.github.trading.infra.adapter.income.web.rest.model.mapper.strategyConfigurationWebIncomeAdapterMapper
import com.github.trading.infra.adapter.income.web.rest.model.request.CreateStrategyConfigurationRequestDto
import com.github.trading.infra.adapter.income.web.rest.model.request.SearchStrategyConfigurationRequestDto
import com.github.trading.infra.adapter.income.web.rest.model.response.StrategyConfigurationSearchResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Strategy configuration", description = "Strategy configuration operations")
@RestController
@RequestMapping("strategy-configurations")
class StrategyConfigurationController(
    private val createStrategyConfigurationUseCase: CreateStrategyConfigurationUseCase,
    private val strategyConfigurationSearchUseCase: SearchStrategyConfigurationUseCase
) {

    @Operation(summary = "Create strategy configuration")
    @PostMapping
    fun create(@RequestBody request: CreateStrategyConfigurationRequestDto) =
        createStrategyConfigurationUseCase.createStrategyConfiguration(
            strategyConfigurationWebIncomeAdapterMapper.map(request)
        )

    @Operation(summary = "Search strategy configurations")
    @PostMapping("search")
    fun search(@RequestBody request: SearchStrategyConfigurationRequestDto) =
        StrategyConfigurationSearchResponseDto(
            strategyConfigurationSearchUseCase.search(
                strategyConfigurationWebIncomeAdapterMapper.map(request)
            ).map(strategyConfigurationWebIncomeAdapterMapper::map)
        )

}