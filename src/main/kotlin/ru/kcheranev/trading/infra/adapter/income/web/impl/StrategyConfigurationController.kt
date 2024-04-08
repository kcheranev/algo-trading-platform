package ru.kcheranev.trading.infra.adapter.income.web.impl

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kcheranev.trading.core.port.income.search.StrategyConfigurationSearchUseCase
import ru.kcheranev.trading.core.port.income.trading.CreateStrategyConfigurationUseCase
import ru.kcheranev.trading.infra.adapter.income.web.model.request.CreateStrategyConfigurationRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StrategyConfigurationSearchRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.model.response.StrategyConfigurationSearchResponseDto
import ru.kcheranev.trading.infra.adapter.income.web.webIncomeAdapterMapper

@Tag(name = "Strategy configuration", description = "Strategy configuration operations")
@RestController
@RequestMapping("strategy-configurations")
class StrategyConfigurationController(
    private val createStrategyConfigurationUseCase: CreateStrategyConfigurationUseCase,
    private val strategyConfigurationSearchUseCase: StrategyConfigurationSearchUseCase
) {

    @Operation(summary = "Create strategy configuration")
    @PostMapping
    fun create(@RequestBody request: CreateStrategyConfigurationRequestDto) =
        createStrategyConfigurationUseCase.createStrategyConfiguration(webIncomeAdapterMapper.map(request))

    @Operation(summary = "Search strategy configurations")
    @PostMapping("search")
    fun search(@RequestBody request: StrategyConfigurationSearchRequestDto) =
        StrategyConfigurationSearchResponseDto(
            strategyConfigurationSearchUseCase.search(webIncomeAdapterMapper.map(request))
                .map { webIncomeAdapterMapper.map(it) }
        )

}