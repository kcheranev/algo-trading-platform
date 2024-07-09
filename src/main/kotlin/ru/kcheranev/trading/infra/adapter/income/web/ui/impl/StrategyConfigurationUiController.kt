package ru.kcheranev.trading.infra.adapter.income.web.ui.impl

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import ru.kcheranev.trading.core.port.income.strategyconfiguration.CreateStrategyConfigurationUseCase
import ru.kcheranev.trading.core.port.income.strategyconfiguration.SearchStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.income.strategyconfiguration.SearchStrategyConfigurationUseCase
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.infra.adapter.income.web.ui.NotFoundException
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper.strategyConfigurationWebIncomeAdapterUiMapper
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.CreateStrategyConfigurationRequestUiDto
import java.util.UUID

@Controller
@RequestMapping("ui/strategy-configurations")
class StrategyConfigurationUiController(
    private val createStrategyConfigurationUseCase: CreateStrategyConfigurationUseCase,
    private val searchStrategyConfigurationUseCase: SearchStrategyConfigurationUseCase
) {

    @PostMapping
    fun create(
        @ModelAttribute request: CreateStrategyConfigurationRequestUiDto,
        bindingResult: BindingResult
    ): String {
        createStrategyConfigurationUseCase.createStrategyConfiguration(
            strategyConfigurationWebIncomeAdapterUiMapper.map(request)
        )
        return "redirect:ui/strategy-configurations"
    }

    @GetMapping
    fun findAll(model: Model): String {
        val strategyConfigurationDtoList =
            searchStrategyConfigurationUseCase.search(SearchStrategyConfigurationCommand())
                .map { strategyConfigurationWebIncomeAdapterUiMapper.map(it) }
        model.addAttribute("strategyConfigurations", strategyConfigurationDtoList)
        return "strategy-configurations"
    }

    @GetMapping("{id}")
    fun findById(
        @PathVariable("id") strategyConfigurationId: UUID,
        model: Model,
        bindingResult: BindingResult
    ): String {
        val strategyConfiguration =
            searchStrategyConfigurationUseCase.search(
                SearchStrategyConfigurationCommand(id = StrategyConfigurationId(strategyConfigurationId))
            )
                .also { if (it.isEmpty()) throw NotFoundException("There is no strategy configuration with id $strategyConfigurationId") }
                .first()
        model.addAttribute(
            "strategyConfiguration",
            strategyConfigurationWebIncomeAdapterUiMapper.map(strategyConfiguration)
        )
        return "strategy-configuration"
    }

}