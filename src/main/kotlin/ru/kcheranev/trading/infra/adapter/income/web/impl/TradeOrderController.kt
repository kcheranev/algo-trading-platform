package ru.kcheranev.trading.infra.adapter.income.web.impl

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kcheranev.trading.core.port.income.search.TradeOrderSearchUseCase
import ru.kcheranev.trading.infra.adapter.income.web.model.request.TradeOrderSearchRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.response.TradeOrderSearchResponse
import ru.kcheranev.trading.infra.adapter.income.web.webIncomeAdapterMapper

@RestController
@RequestMapping("orders")
class TradeOrderController(
    private val tradeOrderSearchUseCase: TradeOrderSearchUseCase
) {

    @PostMapping("search")
    fun search(@RequestBody request: TradeOrderSearchRequest): TradeOrderSearchResponse =
        TradeOrderSearchResponse(
            tradeOrderSearchUseCase.search(webIncomeAdapterMapper.map(request))
                .map { webIncomeAdapterMapper.map(it) }
        )

}