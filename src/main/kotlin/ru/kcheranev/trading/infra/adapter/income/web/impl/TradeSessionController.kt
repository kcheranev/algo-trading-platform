package ru.kcheranev.trading.infra.adapter.income.web.impl

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kcheranev.trading.core.port.income.search.TradeSessionSearchUseCase
import ru.kcheranev.trading.core.port.income.trading.StartTradeSessionUseCase
import ru.kcheranev.trading.core.port.income.trading.StopTradeSessionCommand
import ru.kcheranev.trading.core.port.income.trading.StopTradeSessionUseCase
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.infra.adapter.income.web.model.request.StartTradeSessionRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.request.TradeSessionSearchRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.response.StartTradeSessionResponse
import ru.kcheranev.trading.infra.adapter.income.web.model.response.TradeSessionSearchResponse
import ru.kcheranev.trading.infra.adapter.income.web.webIncomeAdapterMapper

@RestController
@RequestMapping("trade-sessions")
class TradeSessionController(
    private val startTradeSessionUseCase: StartTradeSessionUseCase,
    private val stopTradeSessionUseCase: StopTradeSessionUseCase,
    private val tradeSessionSearchUseCase: TradeSessionSearchUseCase
) {

    @PostMapping
    fun start(@RequestBody request: StartTradeSessionRequest) =
        StartTradeSessionResponse(
            startTradeSessionUseCase.startTradeSession(webIncomeAdapterMapper.map(request)).value
        )

    @PostMapping("{id}/stop")
    fun stop(@PathVariable id: Long) =
        stopTradeSessionUseCase.stopTradeSession(StopTradeSessionCommand(TradeSessionId(id)))


    @PostMapping("search")
    fun search(@RequestBody request: TradeSessionSearchRequest): TradeSessionSearchResponse =
        TradeSessionSearchResponse(
            tradeSessionSearchUseCase.search(webIncomeAdapterMapper.map(request))
                .map { webIncomeAdapterMapper.map(it) }
        )

}