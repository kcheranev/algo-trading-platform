package ru.kcheranev.trading.infra.adapter.income.web.ui.impl

import org.springframework.core.io.FileSystemResource
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.SessionAttributes
import org.springframework.web.multipart.MultipartFile
import ru.kcheranev.trading.core.port.income.backtesting.StrategyAnalyzeUseCase
import ru.kcheranev.trading.core.port.income.instrument.FindAllInstrumentsUseCase
import ru.kcheranev.trading.core.port.income.strategy.GetStrategyParametersNamesCommand
import ru.kcheranev.trading.core.port.income.strategy.GetStrategyParametersNamesUseCase
import ru.kcheranev.trading.core.port.income.strategy.GetStrategyTypesUseCase
import ru.kcheranev.trading.core.util.Validator.Companion.validate
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.backtesting.ProfitTypeSort
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper.backtestingWebIncomeAdapterUiMapper
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper.instrumentWebIncomeAdapterUiMapper
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.CandlesDataSource
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyAnalyzeRequestUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StrategyParameterUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.response.ErrorsUiDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.response.InstrumentUiResponseDto
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.response.StrategyParametersAnalyzeResultUiDto
import ru.kcheranev.trading.infra.config.properties.BacktestingProperties
import java.nio.file.Paths
import kotlin.io.path.createTempFile
import kotlin.io.path.writeBytes

@Controller
@RequestMapping("ui/backtesting")
@SessionAttributes(names = ["candlesSeriesSystemFileName", "candlesSeriesFileName"])
class BacktestingUiController(
    private val strategyAnalyzeUseCase: StrategyAnalyzeUseCase,
    private val getStrategyTypesUseCase: GetStrategyTypesUseCase,
    private val getStrategyParametersNamesUseCase: GetStrategyParametersNamesUseCase,
    private val findAllInstrumentsUseCase: FindAllInstrumentsUseCase,
    backtestingProperties: BacktestingProperties
) {

    private val tempFileDirectory = backtestingProperties.tempFileDirectory

    @ModelAttribute("strategyTypes")
    fun strategyTypes() = getStrategyTypesUseCase.getStrategyTypes()

    @ModelAttribute("profitTypesSort")
    fun profitTypesSort() = ProfitTypeSort.entries.map(ProfitTypeSort::name)

    @ModelAttribute("candleIntervals")
    fun candleIntervals() = CandleInterval.entries.map(CandleInterval::name)

    @ModelAttribute("instruments")
    fun instruments() = findAllInstrumentsUseCase.findAll().map(instrumentWebIncomeAdapterUiMapper::map)

    @GetMapping
    fun analyzeStrategyParameters(model: Model): String {
        model.addAttribute("strategyAnalyzeRequest", StrategyAnalyzeRequestUiDto())
        return "backtesting"
    }

    @PostMapping
    fun analyzeStrategyParameters(
        @ModelAttribute("strategyAnalyzeRequest") request: StrategyAnalyzeRequestUiDto,
        @RequestParam("candlesSeriesFile") candlesSeriesFile: MultipartFile?,
        model: Model,
        bindingResult: BindingResult
    ): String {
        validate {
            with(request) {
                field("strategyType") { strategyType.shouldNotBeNull("Strategy type field must be filled in") }
                strategyParameters.forEach { fieldName, fieldValue ->
                    field(fieldName) { fieldValue.value.shouldNotBeNull("$fieldName field must be filled in") }
                }
                when (candlesSeriesSource) {
                    CandlesDataSource.FILE -> {
                        if (model.getAttribute("candlesSeriesSystemFileName") == null) {
                            field("candlesSeriesFile") {
                                candlesSeriesFile.shouldNotBeNull("Candle series file field must be filled in")
                                candlesSeriesFile?.originalFilename?.shouldNotBeBlank("Candle series file field must be filled in")
                            }
                        }
                    }

                    CandlesDataSource.BROKER -> {
                        field("brokerInstrumentId") { brokerInstrumentId.shouldNotBeNull("Broker instrument id field must be filled in") }
                        field("from") {
                            from.shouldNotBeNull("From field must be filled in")
                        }
                        field("to") {
                            to.shouldNotBeNull("To field must be filled in")
                            if (from != null) {
                                to.shouldBeGreaterThanOrEquals(from, "To field value must be after from field value")
                            }
                        }
                    }
                }
            }
        }.onLeft { validationResult ->
            model.addAttribute("validationErrors", ErrorsUiDto(validationResult.errors, validationResult.fieldErrors))
            model.addAttribute("analyzeResults", emptyList<StrategyParametersAnalyzeResultUiDto>())
            return "backtesting"
        }
        val analyzeResultsDto =
            when (request.candlesSeriesSource) {
                CandlesDataSource.FILE -> {
                    val candlesSeriesFileResource =
                        if (candlesSeriesFile!!.originalFilename!!.isNotEmpty()) {
                            val tempCandleSeriesFile =
                                createTempFile(
                                    directory = Paths.get(tempFileDirectory),
                                    prefix = "candle_series",
                                    suffix = ".json"
                                )
                            tempCandleSeriesFile.writeBytes(candlesSeriesFile.resource.contentAsByteArray)
                            tempCandleSeriesFile.toFile().deleteOnExit()
                            model.addAttribute("candlesSeriesFileName", candlesSeriesFile.originalFilename)
                            model.addAttribute("candlesSeriesSystemFileName", tempCandleSeriesFile.toFile().name)
                            candlesSeriesFile.resource
                        } else {
                            FileSystemResource("$tempFileDirectory/${model.getAttribute("candlesSeriesSystemFileName")}")
                        }
                    strategyAnalyzeUseCase.analyzeStrategyOnStoredData(
                        backtestingWebIncomeAdapterUiMapper.mapToStrategyAnalyzeOnStoredDataCommand(
                            request,
                            candlesSeriesFileResource
                        )
                    ).map(backtestingWebIncomeAdapterUiMapper::map)
                }

                CandlesDataSource.BROKER -> {
                    @Suppress("UNCHECKED_CAST")
                    val ticker =
                        (model.getAttribute("instruments") as List<InstrumentUiResponseDto>)
                            .first { it.brokerInstrumentId == request.brokerInstrumentId }
                            .let(InstrumentUiResponseDto::ticker)
                    strategyAnalyzeUseCase.analyzeStrategyOnBrokerData(
                        backtestingWebIncomeAdapterUiMapper.mapToStrategyAnalyzeOnBrokerDataCommand(
                            request,
                            Instrument(request.brokerInstrumentId!!, ticker)
                        )
                    ).map(backtestingWebIncomeAdapterUiMapper::map)
                }
            }
        model.addAttribute("analyzeResults", analyzeResultsDto)
        return "backtesting"
    }

    @PostMapping(params = ["reloadStrategyParameters"])
    fun reloadStrategyParameters(
        @ModelAttribute("strategyAnalyzeRequest") request: StrategyAnalyzeRequestUiDto,
        bindingResult: BindingResult
    ): String {
        request.strategyParameters.clear()
        getStrategyParametersNamesUseCase.getStrategyParametersNames(
            GetStrategyParametersNamesCommand(request.strategyType!!)
        ).forEach { request.strategyParameters[it] = StrategyParameterUiDto() }
        return "backtesting"
    }

}