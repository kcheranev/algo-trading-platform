package ru.kcheranev.trading.infra.adapter.income.web.ui.impl.backtesting

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeUseCase
import ru.kcheranev.trading.core.port.income.instrument.FindAllInstrumentsUseCase
import ru.kcheranev.trading.core.port.income.strategy.GetStrategyParametersNamesCommand
import ru.kcheranev.trading.core.port.income.strategy.GetStrategyParametersNamesUseCase
import ru.kcheranev.trading.core.port.income.strategy.GetStrategyTypesUseCase
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.backtesting.ProfitTypeSort
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.common.InstrumentUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper.backtestingWebIncomeAdapterUiMapper
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper.commonModelUiMapper
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyParameterUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyParametersAnalyzeRequestUiDto

@Controller
@RequestMapping("ui/backtesting")
class BacktestingStrategyParametersAnalyzeUiController(
    private val strategyAnalyzeUseCase: StrategyAnalyzeUseCase,
    private val getStrategyTypesUseCase: GetStrategyTypesUseCase,
    private val getStrategyParametersNamesUseCase: GetStrategyParametersNamesUseCase,
    private val findAllInstrumentsUseCase: FindAllInstrumentsUseCase
) {

    @ModelAttribute("strategyTypes")
    fun strategyTypes() = getStrategyTypesUseCase.getStrategyTypes()

    @ModelAttribute("profitTypesSort")
    fun profitTypesSort() = ProfitTypeSort.entries.map(ProfitTypeSort::name)

    @ModelAttribute("candleIntervals")
    fun candleIntervals() = CandleInterval.entries.map(CandleInterval::name)

    @ModelAttribute("instruments")
    fun instruments() = findAllInstrumentsUseCase.findAll().map(commonModelUiMapper::map)

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
        @Suppress("UNCHECKED_CAST")
        val ticker =
            (model.getAttribute("instruments") as List<InstrumentUiDto>)
                .first { it.brokerInstrumentId == request.brokerInstrumentId }
                .let(InstrumentUiDto::ticker)
        val analyzeResultsDto =
            strategyAnalyzeUseCase.analyzeStrategyParameters(
                backtestingWebIncomeAdapterUiMapper.map(request, Instrument(request.brokerInstrumentId!!, ticker))
            ).map(backtestingWebIncomeAdapterUiMapper::map)
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