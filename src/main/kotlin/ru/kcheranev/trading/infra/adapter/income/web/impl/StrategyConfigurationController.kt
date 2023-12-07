package ru.kcheranev.trading.infra.adapter.income.web.impl

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kcheranev.trading.core.port.income.search.StrategyConfigurationSearchUseCase
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StrategyConfigurationSearchRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.response.StrategyConfigurationSearchResponse
import ru.kcheranev.trading.infra.adapter.income.web.webIncomeAdapterMapper

@RestController
@RequestMapping("strategy-configurations")
class StrategyConfigurationController(
    private val strategyConfigurationSearchUseCase: StrategyConfigurationSearchUseCase
) {

    @PostMapping("search")
    fun search(request: StrategyConfigurationSearchRequest): StrategyConfigurationSearchResponse {
        return StrategyConfigurationSearchResponse(
            strategyConfigurationSearchUseCase.search(webIncomeAdapterMapper.map(request))
                .map { webIncomeAdapterMapper.map(it) }
        )
    }

}