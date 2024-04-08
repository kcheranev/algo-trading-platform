package ru.kcheranev.trading.infra.adapter.income.web.impl

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kcheranev.trading.core.port.income.search.TradeOrderSearchUseCase
import ru.kcheranev.trading.infra.adapter.income.web.model.request.TradeOrderSearchRequestDto
import ru.kcheranev.trading.infra.adapter.income.web.model.response.TradeOrderSearchResponseDto
import ru.kcheranev.trading.infra.adapter.income.web.webIncomeAdapterMapper

@Tag(name = "Trade order", description = "Trade order operations")
@RestController
@RequestMapping("trade-orders")
class TradeOrderController(
    private val tradeOrderSearchUseCase: TradeOrderSearchUseCase
) {

    @Operation(summary = "Search trade orders")
    @PostMapping("search")
    fun search(@RequestBody request: TradeOrderSearchRequestDto) =
        TradeOrderSearchResponseDto(
            tradeOrderSearchUseCase.search(webIncomeAdapterMapper.map(request))
                .map { webIncomeAdapterMapper.map(it) }
        )

}