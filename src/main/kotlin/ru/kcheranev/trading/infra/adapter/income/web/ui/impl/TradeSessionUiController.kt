package ru.kcheranev.trading.infra.adapter.income.web.ui.impl

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import ru.kcheranev.trading.core.port.income.tradesession.SearchTradeSessionCommand
import ru.kcheranev.trading.core.port.income.tradesession.SearchTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.tradesession.StartTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.tradesession.StopTradeSessionCommand
import ru.kcheranev.trading.core.port.income.tradesession.StopTradeSessionUseCase
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.infra.adapter.income.web.ui.NotFoundException
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.mapper.tradeSessionWebIncomeAdapterUiMapper
import ru.kcheranev.trading.infra.adapter.income.web.ui.model.request.StartTradeSessionRequestUiDto
import java.util.UUID

@Controller
@RequestMapping("ui/trade-sessions")
class TradeSessionUiController(
    private val startTradeSessionUseCase: StartTradeSessionUseCase,
    private val stopTradeSessionUseCase: StopTradeSessionUseCase,
    private val searchTradeSessionUseCase: SearchTradeSessionUseCase
) {

    @PostMapping
    fun start(
        @RequestBody request: StartTradeSessionRequestUiDto,
        bindingResult: BindingResult
    ): String {
        startTradeSessionUseCase.startTradeSession(tradeSessionWebIncomeAdapterUiMapper.map(request))
        return "redirect:ui/trade-sessions"
    }

    @PostMapping("{id}/stop")
    fun stop(@PathVariable id: UUID, bindingResult: BindingResult): String {
        stopTradeSessionUseCase.stopTradeSession(StopTradeSessionCommand(TradeSessionId(id)))
        return "redirect:ui/trade-sessions"
    }

    @GetMapping
    fun findAll(model: Model): String {
        val tradeSessionDtoList =
            searchTradeSessionUseCase.search(SearchTradeSessionCommand())
                .map { tradeSessionWebIncomeAdapterUiMapper.map(it) }
        model.addAttribute("tradeSessions", tradeSessionDtoList)
        return "trade-sessions"
    }

    @GetMapping("{id}")
    fun findById(
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