package ru.kcheranev.trading.infra.adapter.income.web.impl

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kcheranev.trading.core.port.income.search.OrderSearchUseCase
import ru.kcheranev.trading.infra.adapter.income.web.model.request.OrderSearchRequest
import ru.kcheranev.trading.infra.adapter.income.web.model.response.OrderSearchResponse
import ru.kcheranev.trading.infra.adapter.income.web.webIncomeAdapterMapper

@RestController
@RequestMapping("orders")
class OrderController(
    private val orderSearchUseCase: OrderSearchUseCase
) {

    @PostMapping("search")
    fun search(request: OrderSearchRequest): OrderSearchResponse {
        return OrderSearchResponse(
            orderSearchUseCase.search(webIncomeAdapterMapper.map(request))
                .map { webIncomeAdapterMapper.map(it) }
        )
    }

}