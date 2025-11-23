package com.github.trading.infra.adapter.income.web.ui.impl

import com.github.trading.core.port.income.tradesession.CreateTradeSessionUseCase
import com.github.trading.core.port.income.tradesession.ResumeTradeSessionCommand
import com.github.trading.core.port.income.tradesession.ResumeTradeSessionUseCase
import com.github.trading.core.port.income.tradesession.SearchTradeSessionCommand
import com.github.trading.core.port.income.tradesession.SearchTradeSessionUseCase
import com.github.trading.core.port.income.tradesession.StopTradeSessionCommand
import com.github.trading.core.port.income.tradesession.StopTradeSessionUseCase
import com.github.trading.domain.entity.TradeSessionId
import com.github.trading.infra.adapter.income.web.ui.NotFoundException
import com.github.trading.infra.adapter.income.web.ui.model.mapper.tradeSessionWebIncomeAdapterUiMapper
import com.github.trading.infra.adapter.income.web.ui.model.request.CreateTradeSessionRequestUiDto
import com.github.trading.infra.adapter.income.web.ui.model.response.TradeSessionUiDto
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import java.util.UUID

@Controller
@RequestMapping("ui/trade-sessions")
class TradeSessionUiController(
    private val createTradeSessionUseCase: CreateTradeSessionUseCase,
    private val stopTradeSessionUseCase: StopTradeSessionUseCase,
    private val resumeTradeSessionUseCase: ResumeTradeSessionUseCase,
    private val searchTradeSessionUseCase: SearchTradeSessionUseCase
) {

    @PostMapping
    fun create(
        @RequestBody request: CreateTradeSessionRequestUiDto,
        bindingResult: BindingResult
    ): String {
        createTradeSessionUseCase.createTradeSession(tradeSessionWebIncomeAdapterUiMapper.map(request))
        return "redirect:/ui/trade-sessions"
    }

    @PostMapping("{id}/stop")
    fun stop(@PathVariable("id") tradeSessionId: UUID): String {
        stopTradeSessionUseCase.stopTradeSession(StopTradeSessionCommand(TradeSessionId(tradeSessionId)))
        return "redirect:/ui/trade-sessions"
    }

    @PostMapping("{id}/resume")
    fun resume(@PathVariable("id") tradeSessionId: UUID): String {
        resumeTradeSessionUseCase.resumeTradeSession(ResumeTradeSessionCommand(TradeSessionId(tradeSessionId)))
        return "redirect:/ui/trade-sessions"
    }

    @GetMapping
    fun findAll(model: Model): String {
        val tradeSessionDtoList =
            searchTradeSessionUseCase.search(SearchTradeSessionCommand())
                .map(tradeSessionWebIncomeAdapterUiMapper::map)
                .sortedBy(TradeSessionUiDto::ticker)
        model.addAttribute("tradeSessions", tradeSessionDtoList)
        return "trade-sessions"
    }

    @GetMapping("{id}")
    fun getById(
        @PathVariable("id") tradeSessionId: UUID,
        model: Model,
        bindingResult: BindingResult
    ): String {
        val tradeSession =
            searchTradeSessionUseCase.search(SearchTradeSessionCommand(id = TradeSessionId(tradeSessionId)))
                .also { if (it.isEmpty()) throw NotFoundException("There is no trade session with id $tradeSessionId") }
                .first()
        model.addAttribute(
            "tradeSession",
            tradeSessionWebIncomeAdapterUiMapper.map(tradeSession)
        )
        return "trade-session"
    }

}