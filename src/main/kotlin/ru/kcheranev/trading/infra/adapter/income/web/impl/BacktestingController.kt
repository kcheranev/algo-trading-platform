package ru.kcheranev.trading.infra.adapter.income.web.impl

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeUseCase
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StrategyAnalyzeRequest
import ru.kcheranev.trading.infra.adapter.income.web.webIncomeAdapterMapper

@Tag(name = "Backtesting")
@RestController
@RequestMapping("backtesting")
class BacktestingController(
    private val strategyAnalyzeUseCase: StrategyAnalyzeUseCase
) {

    @Operation(summary = "Analyze trade strategy")
    @PostMapping
    fun analyzeStrategy(@RequestBody request: StrategyAnalyzeRequest) =
        webIncomeAdapterMapper.map(strategyAnalyzeUseCase.analyzeStrategy(webIncomeAdapterMapper.map(request)))

}