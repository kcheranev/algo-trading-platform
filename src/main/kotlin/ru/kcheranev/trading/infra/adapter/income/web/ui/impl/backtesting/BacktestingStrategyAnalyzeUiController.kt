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
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper.backtestingWebIncomeAdapterUiMapper
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper.instrumentWebIncomeAdapterUiMapper
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.CandlesDataSource
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyAnalyzeRequestUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.response.InstrumentUiResponseDto

@Controller
@RequestMapping("ui/backtesting")
class BacktestingStrategyAnalyzeUiController(
    private val strategyAnalyzeUseCase: StrategyAnalyzeUseCase,
    private val getStrategyTypesUseCase: GetStrategyTypesUseCase,
    private val getStrategyParametersNamesUseCase: GetStrategyParametersNamesUseCase,
    private val findAllInstrumentsUseCase: FindAllInstrumentsUseCase
) {

    @ModelAttribute("strategyTypes")
    fun strategyTypes() = getStrategyTypesUseCase.getStrategyTypes()

    @ModelAttribute("candleIntervals")
    fun candleIntervals() = CandleInterval.entries.map(CandleInterval::name)

    @ModelAttribute("instruments")
    fun instruments() = findAllInstrumentsUseCase.findAll().map(instrumentWebIncomeAdapterUiMapper::map)

    @GetMapping("analyze")
    fun analyzeStrategy(model: Model): String {
        model.addAttribute("analyzeStrategyRequest", StrategyAnalyzeRequestUiDto())
        return "backtesting/analyze"
    }

    @PostMapping("analyze")
    fun analyzeStrategy(
        @ModelAttribute("analyzeStrategyRequest") request: StrategyAnalyzeRequestUiDto,
        @RequestParam("candlesSeriesFile") candlesSeriesFile: MultipartFile?,
        model: Model,
        bindingResult: BindingResult
    ): String {
        val analyzeResultDto =
            when (request.candlesSeriesSource) {
                CandlesDataSource.FILE ->
                    backtestingWebIncomeAdapterUiMapper.map(
                        strategyAnalyzeUseCase.analyzeStrategyOnStoredData(
                            backtestingWebIncomeAdapterUiMapper.mapToStrategyAnalyzeOnStoredDataCommand(
                                request,
                                candlesSeriesFile!!.resource
                            )
                        )
                    )

                CandlesDataSource.BROKER -> {
                    @Suppress("UNCHECKED_CAST")
                    val ticker =
                        (model.getAttribute("instruments") as List<InstrumentUiResponseDto>)
                            .first { it.brokerInstrumentId == request.brokerInstrumentId }
                            .let(InstrumentUiResponseDto::ticker)
                    backtestingWebIncomeAdapterUiMapper.map(
                        strategyAnalyzeUseCase.analyzeStrategyOnBrokerData(
                            backtestingWebIncomeAdapterUiMapper.mapToStrategyAnalyzeOnBrokerDataCommand(
                                request,
                                Instrument(request.brokerInstrumentId!!, ticker)
                            )
                        )
                    )
                }
            }
        model.addAttribute("analyzeResult", analyzeResultDto)
        return "backtesting/analyze"
    }

    @PostMapping(value = ["analyze"], params = ["reloadStrategyParameters"])
    fun reloadStrategyParameters(
        @ModelAttribute("analyzeStrategyRequest") request: StrategyAnalyzeRequestUiDto,
        bindingResult: BindingResult
    ): String {
        request.strategyParameters.clear()
        getStrategyParametersNamesUseCase.getStrategyParametersNames(
            GetStrategyParametersNamesCommand(request.strategyType!!)
        ).forEach { request.strategyParameters[it] = null }
        return "backtesting/analyze"
    }

}