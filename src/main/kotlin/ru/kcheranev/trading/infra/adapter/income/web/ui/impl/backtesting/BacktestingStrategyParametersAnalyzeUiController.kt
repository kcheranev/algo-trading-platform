package ru.kcheranev.trading.infra.adapter.income.web.ui.impl.backtesting

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeUseCase
import ru.kcheranev.trading.core.port.income.instrument.FindAllInstrumentsUseCase
import ru.kcheranev.trading.core.port.income.strategy.GetStrategyParametersNamesCommand
import ru.kcheranev.trading.core.port.income.strategy.GetStrategyParametersNamesUseCase
import ru.kcheranev.trading.core.port.income.strategy.GetStrategyTypesUseCase
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.backtesting.ProfitTypeSort
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper.backtestingWebIncomeAdapterUiMapper
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper.instrumentWebIncomeAdapterUiMapper
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.CandlesDataSource
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyParameterUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyParametersAnalyzeRequestUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.response.InstrumentUiResponseDto

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
    fun instruments() = findAllInstrumentsUseCase.findAll().map(instrumentWebIncomeAdapterUiMapper::map)

    @GetMapping("parameters-analyze")
    fun analyzeStrategyParameters(model: Model): String {
        model.addAttribute("strategyParametersAnalyzeRequest", StrategyParametersAnalyzeRequestUiDto())
        return "backtesting/parameters-analyze"
    }

    @PostMapping("parameters-analyze")
    fun analyzeStrategyParameters(
        @ModelAttribute("strategyParametersAnalyzeRequest") request: StrategyParametersAnalyzeRequestUiDto,
        @RequestParam("candlesSeriesFile") candlesSeriesFile: MultipartFile?,
        model: Model,
        bindingResult: BindingResult
    ): String {
        val analyzeResultsDto =
            when (request.candlesSeriesSource) {
                CandlesDataSource.FILE ->
                    strategyAnalyzeUseCase.analyzeStrategyParametersOnStoredData(
                        backtestingWebIncomeAdapterUiMapper.mapToStrategyParametersAnalyzeOnStoredDataCommand(
                            request,
                            candlesSeriesFile!!.resource
                        )
                    ).map(backtestingWebIncomeAdapterUiMapper::map)

                CandlesDataSource.BROKER -> {
                    @Suppress("UNCHECKED_CAST")
                    val ticker =
                        (model.getAttribute("instruments") as List<InstrumentUiResponseDto>)
                            .first { it.brokerInstrumentId == request.brokerInstrumentId }
                            .let(InstrumentUiResponseDto::ticker)
                    strategyAnalyzeUseCase.analyzeStrategyParametersOnBrokerData(
                        backtestingWebIncomeAdapterUiMapper.mapToStrategyParametersAnalyzeOnBrokerDataCommand(
                            request,
                            Instrument(request.brokerInstrumentId!!, ticker)
                        )
                    ).map(backtestingWebIncomeAdapterUiMapper::map)
                }
            }
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