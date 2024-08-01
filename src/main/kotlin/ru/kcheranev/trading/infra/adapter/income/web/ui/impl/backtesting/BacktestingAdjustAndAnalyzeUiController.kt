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
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyAdjustAndAnalyzeRequestUiDto

@Controller
@RequestMapping("ui/backtesting")
class BacktestingAdjustAndAnalyzeUiController(
    private val strategyAnalyzeUseCase: StrategyAnalyzeUseCase,
    private val getStrategyTypesUseCase: GetStrategyTypesUseCase
) {

    @GetMapping("adjust-and-analyze")
    fun adjustAndAnalyzeStrategy(model: Model) = "backtesting/adjust-and-analyze"

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