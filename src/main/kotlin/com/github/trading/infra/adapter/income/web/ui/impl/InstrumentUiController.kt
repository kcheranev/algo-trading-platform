package com.github.trading.infra.adapter.income.web.ui.impl

import com.github.trading.core.port.income.instrument.CreateInstrumentUseCase
import com.github.trading.core.port.income.instrument.FindAllInstrumentsUseCase
import com.github.trading.infra.adapter.income.web.ui.model.mapper.instrumentWebIncomeAdapterUiMapper
import com.github.trading.infra.adapter.income.web.ui.model.request.CreateInstrumentRequestUiDto
import com.github.trading.infra.adapter.income.web.ui.model.response.InstrumentUiResponseDto
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("ui/instruments")
class InstrumentUiController(
    private val createInstrumentUseCase: CreateInstrumentUseCase,
    private val findAllInstrumentsUseCase: FindAllInstrumentsUseCase
) {

    @PostMapping
    fun create(
        @ModelAttribute("createInstrumentRequest") request: CreateInstrumentRequestUiDto,
        bindingResult: BindingResult
    ): String {
        createInstrumentUseCase.createInstrument(
            instrumentWebIncomeAdapterUiMapper.map(request)
        )
        return "redirect:/ui/instruments"
    }

    @GetMapping
    fun findAll(model: Model): String {
        val instruments =
            findAllInstrumentsUseCase.findAll()
                .map(instrumentWebIncomeAdapterUiMapper::map)
                .sortedBy(InstrumentUiResponseDto::ticker)
        model.addAttribute("instruments", instruments)
        model.addAttribute("createInstrumentRequest", CreateInstrumentRequestUiDto())
        return "instruments"
    }

}