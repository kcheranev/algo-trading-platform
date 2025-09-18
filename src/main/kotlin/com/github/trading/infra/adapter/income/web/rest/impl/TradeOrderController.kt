package com.github.trading.infra.adapter.income.web.rest.impl

import com.github.trading.core.port.income.tradeorder.SearchTradeOrderUseCase
import com.github.trading.infra.adapter.income.web.rest.model.mapper.tradeOrderWebIncomeAdapterMapper
import com.github.trading.infra.adapter.income.web.rest.model.request.SearchTradeOrderRequestDto
import com.github.trading.infra.adapter.income.web.rest.model.response.TradeOrderSearchResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Trade order", description = "Trade order operations")
@RestController
@RequestMapping("trade-orders")
class TradeOrderController(
    private val tradeOrderSearchUseCase: SearchTradeOrderUseCase
) {

    @Operation(summary = "Search trade orders")
    @PostMapping("search")
    fun search(@RequestBody request: SearchTradeOrderRequestDto) =
        TradeOrderSearchResponseDto(
            tradeOrderSearchUseCase.search(tradeOrderWebIncomeAdapterMapper.map(request))
                .map(tradeOrderWebIncomeAdapterMapper::map)
        )

}