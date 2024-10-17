package ru.kcheranev.trading.infra.adapter.income.web.ui.impl.backtesting

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeUseCase
import ru.kcheranev.trading.core.port.income.strategy.GetStrategyTypesUseCase
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper.backtestingWebIncomeAdapterUiMapper
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyParametersAnalyzeRequestUiDto

@Controller
@RequestMapping("ui/backtesting")
class BacktestingStrategyParametersAnalyzeUiController(
    private val strategyAnalyzeUseCase: StrategyAnalyzeUseCase,
    private val getStrategyTypesUseCase: GetStrategyTypesUseCase
) {

    @GetMapping("analyze-parameters")
    fun analyzeStrategyParameters(model: Model) = "backtesting/analyze-parameters"

    @PostMapping("analyze-parameters")
    fun analyzeStrategyParameters(
        @ModelAttribute request: StrategyParametersAnalyzeRequestUiDto,
        model: Model,
        bindingResult: BindingResult
    ): String {
        val analyzeResultDto =
            strategyAnalyzeUseCase.analyzeStrategyParameters(backtestingWebIncomeAdapterUiMapper.map(request))
                .map { backtestingWebIncomeAdapterUiMapper.map(it) }
        model.addAttribute("analyzeResult", analyzeResultDto)
        return "backtesting/analyze-parameters"
    }

}