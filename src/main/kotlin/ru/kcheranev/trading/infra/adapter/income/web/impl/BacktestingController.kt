package ru.kcheranev.trading.infra.adapter.income.web.impl

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeUseCase
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StrategyAdjustAndAnalyzeRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StrategyAnalyzeRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.model.response.StrategyAdjustAndAnalyzeResponseDto
import ru.kcheranev.trading.infra.adapter.income.web.webIncomeAdapterMapper

@Tag(name = "Backtesting")
@RestController
@RequestMapping("backtesting")
class BacktestingController(
    private val strategyAnalyzeUseCase: StrategyAnalyzeUseCase
) {

    @Operation(summary = "Analyze trade strategy")
    @PostMapping("analyze")
    fun analyzeStrategy(@RequestBody request: StrategyAnalyzeRequestDto) =
        webIncomeAdapterMapper.map(strategyAnalyzeUseCase.analyzeStrategy(webIncomeAdapterMapper.map(request)))

    @Operation(summary = "Adjust and analyze trade strategy")
    @PostMapping("adjust-and-analyze")
    fun adjustAndAnalyzeStrategy(@RequestBody request: StrategyAdjustAndAnalyzeRequestDto) =
        StrategyAdjustAndAnalyzeResponseDto(
            strategyAnalyzeUseCase.adjustAndAnalyzeStrategy(webIncomeAdapterMapper.map(request))
                .map { webIncomeAdapterMapper.map(it) }
        )

}