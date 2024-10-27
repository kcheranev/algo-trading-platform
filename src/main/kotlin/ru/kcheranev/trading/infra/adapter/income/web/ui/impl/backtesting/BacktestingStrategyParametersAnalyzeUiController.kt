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
import ru.kcheranev.trading.domain.model.backtesting.ProfitTypeSort
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper.backtestingWebIncomeAdapterUiMapper
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyParameterUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyParametersAnalyzeRequestUiDto

@Controller
@RequestMapping("ui/backtesting")
class BacktestingStrategyParametersAnalyzeUiController(
    private val strategyAnalyzeUseCase: StrategyAnalyzeUseCase,
    private val getStrategyTypesUseCase: GetStrategyTypesUseCase,
    private val getStrategyParametersNamesUseCase: GetStrategyParametersNamesUseCase
) {

    @ModelAttribute("strategyTypes")
    fun strategyTypes() = getStrategyTypesUseCase.getStrategyTypes()

    @ModelAttribute("profitTypesSort")
    fun profitTypesSort() = ProfitTypeSort.values().map { it.name }

    @ModelAttribute("candleIntervals")
    fun candleIntervals() = CandleInterval.values().map { it.name }

    @GetMapping("parameters-analyze")
    fun analyzeStrategyParameters(model: Model): String {
        model.addAttribute("strategyParametersAnalyzeRequest", StrategyParametersAnalyzeRequestUiDto())
        return "backtesting/parameters-analyze"
    }

    @PostMapping("parameters-analyze")
    fun analyzeStrategyParameters(
        @ModelAttribute("strategyParametersAnalyzeRequest") request: StrategyParametersAnalyzeRequestUiDto,
        model: Model,
        bindingResult: BindingResult
    ): String {
        val analyzeResultsDto =
            strategyAnalyzeUseCase.analyzeStrategyParameters(
                backtestingWebIncomeAdapterUiMapper.map(request)
            ).map { backtestingWebIncomeAdapterUiMapper.map(it) }
        model.addAttribute("analyzeResults", analyzeResultsDto)
        return "backtesting/parameters-analyze"
    }

    @PostMapping(value = ["parameters-analyze"], params = ["reloadStrategyParameters"])
    fun reloadStrategyParameters(
        @ModelAttribute("strategyParametersAnalyzeRequest") request: StrategyParametersAnalyzeRequestUiDto,
        bindingResult: BindingResult
    ): String {
        request.strategyParameters.clear()
        getStrategyParametersNamesUseCase.getStrategyParametersNames(
            GetStrategyParametersNamesCommand(request.strategyType!!)
        ).forEach { request.strategyParameters[it] = StrategyParameterUiDto() }
        return "backtesting/parameters-analyze"
    }

}