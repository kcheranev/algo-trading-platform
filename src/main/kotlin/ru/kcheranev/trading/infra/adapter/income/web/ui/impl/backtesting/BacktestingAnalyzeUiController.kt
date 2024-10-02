package ru.kcheranev.trading.infra.adapter.income.web.ui.impl.backtesting

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeUseCase
import ru.kcheranev.trading.core.port.income.strategy.GetStrategyParametersNamesCommand
import ru.kcheranev.trading.core.port.income.strategy.GetStrategyParametersNamesUseCase
import ru.kcheranev.trading.core.port.income.strategy.GetStrategyTypesUseCase
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper.backtestingWebIncomeAdapterUiMapper
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyAnalyzeRequestUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyParameterUiDto

@Controller
@RequestMapping("ui/backtesting")
class BacktestingAnalyzeUiController(
    private val strategyAnalyzeUseCase: StrategyAnalyzeUseCase,
    private val getStrategyTypesUseCase: GetStrategyTypesUseCase,
    private val getStrategyParametersNamesUseCase: GetStrategyParametersNamesUseCase
) {

    @ModelAttribute("strategyTypes")
    fun strategyTypes() = getStrategyTypesUseCase.getStrategyTypes()

    @ModelAttribute("candleIntervals")
    fun candleIntervals() = CandleInterval.values().map { it.name }

    @GetMapping("analyze")
    fun analyzeStrategy(model: Model): String {
        model.addAttribute("analyzeStrategyRequest", StrategyAnalyzeRequestUiDto())
        return "backtesting/analyze"
    }

    @PostMapping("analyze")
    fun analyzeStrategy(
        @ModelAttribute("analyzeStrategyRequest") analyzeStrategyRequest: StrategyAnalyzeRequestUiDto,
        model: Model,
        bindingResult: BindingResult
    ): String {
        val analyzeResultDto =
            backtestingWebIncomeAdapterUiMapper.map(
                strategyAnalyzeUseCase.analyzeStrategy(backtestingWebIncomeAdapterUiMapper.map(analyzeStrategyRequest))
            )
        model.addAttribute("analyzeResult", analyzeResultDto)
        return "backtesting/analyze"
    }

    @PostMapping(value = ["analyze"], params = ["reloadStrategyParameters"])
    fun strategyParameterNames(
        @ModelAttribute("analyzeStrategyRequest") analyzeStrategyRequest: StrategyAnalyzeRequestUiDto,
        bindingResult: BindingResult
    ): String {
        analyzeStrategyRequest.strategyParameters.clear()
        getStrategyParametersNamesUseCase.getStrategyParametersNames(
            GetStrategyParametersNamesCommand(analyzeStrategyRequest.strategyType!!)
        ).forEach { analyzeStrategyRequest.strategyParameters.add(StrategyParameterUiDto(it)) }
        return "backtesting/analyze"
    }

}