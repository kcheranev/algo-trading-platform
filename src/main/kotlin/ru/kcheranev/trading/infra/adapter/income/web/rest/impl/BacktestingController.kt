package ru.kcheranev.trading.infra.adapter.income.web.rest.impl

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeUseCase
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.mapper.backtestingWebIncomeAdapterMapper
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.request.StrategyAnalyzeRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.request.StrategyParametersAnalyzeRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.response.StrategyAnalyzeResponseDto
import ru.kcheranev.trading.infra.adapter.income.web.rest.model.response.StrategyParametersAnalyzeResponseDto

@Tag(name = "Backtesting")
@RestController
@RequestMapping("backtesting")
class BacktestingController(
    private val strategyAnalyzeUseCase: StrategyAnalyzeUseCase
) {

    @Operation(summary = "Analyze trade strategy")
    @PostMapping("analyze")
    fun analyzeStrategy(@RequestBody request: StrategyAnalyzeRequestDto) =
        StrategyAnalyzeResponseDto(
            backtestingWebIncomeAdapterMapper.map(
                strategyAnalyzeUseCase.analyzeStrategy(backtestingWebIncomeAdapterMapper.map(request))
            )
        )

    @Operation(summary = "Analyze strategy parameters")
    @PostMapping("analyze-parameters")
    fun analyzeStrategyParameters(@RequestBody request: StrategyParametersAnalyzeRequestDto) =
        StrategyParametersAnalyzeResponseDto(
            strategyAnalyzeUseCase.analyzeStrategyParameters(backtestingWebIncomeAdapterMapper.map(request))
                .map(backtestingWebIncomeAdapterMapper::map)
        )

}