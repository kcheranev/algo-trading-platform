package ru.kcheranev.trading.infra.adapter.income.web.impl

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kcheranev.trading.core.port.income.search.TradeSessionSearchUseCase
import ru.kcheranev.trading.infra.adapter.income.web.model.request.TradeSessionSearchRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.response.TradeSessionSearchResponse
import ru.kcheranev.trading.infra.adapter.income.web.webIncomeAdapterMapper

@RestController
@RequestMapping("trade-sessions")
class TradeSessionController(
    private val tradeSessionSearchUseCase: TradeSessionSearchUseCase
) {

    @PostMapping("search")
    fun search(request: TradeSessionSearchRequest): TradeSessionSearchResponse {
        return TradeSessionSearchResponse(
            tradeSessionSearchUseCase.search(webIncomeAdapterMapper.map(request))
                .map { webIncomeAdapterMapper.map(it) }
        )
    }

}