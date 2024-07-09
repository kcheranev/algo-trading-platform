package ru.kcheranev.trading.infra.adapter.income.web.ui.impl

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeUseCase
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper.backtestingWebIncomeAdapterUiMapper
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyAdjustAndAnalyzeRequestUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyAnalyzeRequestUiDto

@Controller
@RequestMapping("ui/backtesting")
class BacktestingUiController(
    private val strategyAnalyzeUseCase: StrategyAnalyzeUseCase
) {

    @PostMapping("analyze")
    fun analyzeStrategy(
        @ModelAttribute request: StrategyAnalyzeRequestUiDto,
        model: Model,
        bindingResult: BindingResult
    ): String {
        val analyzeResultDto =
            backtestingWebIncomeAdapterUiMapper.map(
                strategyAnalyzeUseCase.analyzeStrategy(backtestingWebIncomeAdapterUiMapper.map(request))
            )
        model.addAttribute("analyzeResult", analyzeResultDto)
        return "backtesting/analyze"
    }

    @PostMapping("adjust-and-analyze")
    fun adjustAndAnalyzeStrategy(
        @ModelAttribute request: StrategyAdjustAndAnalyzeRequestUiDto,
        model: Model,
        bindingResult: BindingResult
    ): String {
        val analyzeResultDto =
            strategyAnalyzeUseCase.adjustAndAnalyzeStrategy(backtestingWebIncomeAdapterUiMapper.map(request))
                .map { backtestingWebIncomeAdapterUiMapper.map(it) }
        model.addAttribute("analyzeResult", analyzeResultDto)
        return "backtesting/adjust-and-analyze"
    }

}