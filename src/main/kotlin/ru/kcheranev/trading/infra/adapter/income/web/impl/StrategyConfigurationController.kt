package ru.kcheranev.trading.infra.adapter.income.web.impl

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kcheranev.trading.core.port.income.search.StrategyConfigurationSearchUseCase
import ru.kcheranev.trading.core.port.income.trading.CreateStrategyConfigurationUseCase
import ru.kcheranev.trading.infra.adapter.income.web.model.request.CreateStrategyConfigurationRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StrategyConfigurationSearchRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.response.StrategyConfigurationSearchResponse
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
    fun create(@RequestBody request: CreateStrategyConfigurationRequest) =
        createStrategyConfigurationUseCase.createStrategyConfiguration(webIncomeAdapterMapper.map(request))

    @Operation(summary = "Search strategy configurations")
    @PostMapping("search")
    fun search(@RequestBody request: StrategyConfigurationSearchRequest): StrategyConfigurationSearchResponse =
        StrategyConfigurationSearchResponse(
            strategyConfigurationSearchUseCase.search(webIncomeAdapterMapper.map(request))
                .map { webIncomeAdapterMapper.map(it) }
        )

}