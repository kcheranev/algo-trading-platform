package com.github.trading.infra.adapter.income.web.rest.impl

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.github.trading.core.port.income.historiccandles.StoreHistoricCandlesUseCase
import com.github.trading.infra.adapter.income.web.rest.model.mapper.historicCandlesWebIncomeAdapterMapper
import com.github.trading.infra.adapter.income.web.rest.model.request.StoreHistoricCandlesRequestDto

@Tag(name = "Historic candles", description = "Load historic candles")
@RestController
@RequestMapping("historic-candles")
class HistoricCandleController(
    private val storeHistoricCandlesUseCase: StoreHistoricCandlesUseCase
) {

    @Operation(summary = "Load historic candles")
    @PostMapping("store")
    fun storeHistoricCandles(@RequestBody request: StoreHistoricCandlesRequestDto) {
        storeHistoricCandlesUseCase.storeHistoricCandles(historicCandlesWebIncomeAdapterMapper.map(request))
    }

}