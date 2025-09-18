package com.github.trading.infra.adapter.income.web.rest.impl

import com.github.trading.core.port.income.backtesting.StrategyAnalyzeUseCase
import com.github.trading.infra.adapter.income.web.rest.model.mapper.backtestingWebIncomeAdapterMapper
import com.github.trading.infra.adapter.income.web.rest.model.request.StrategyAnalyzeRequestDto
import com.github.trading.infra.adapter.income.web.rest.model.response.StrategyAnalyzeResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Backtesting")
@RestController
@RequestMapping("backtesting")
class BacktestingController(
    private val strategyAnalyzeUseCase: StrategyAnalyzeUseCase
) {

    @Operation(summary = "Analyze strategy")
    @PostMapping
    fun analyzeStrategyParameters(@RequestBody request: StrategyAnalyzeRequestDto) =
        StrategyAnalyzeResponseDto(
            strategyAnalyzeUseCase.analyzeStrategyOnBrokerData(backtestingWebIncomeAdapterMapper.map(request))
                .map(backtestingWebIncomeAdapterMapper::map)
        )

}