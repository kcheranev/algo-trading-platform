package com.github.trading.infra.adapter.income.web.rest.model.request

import com.github.trading.core.port.model.Page
import com.github.trading.core.port.model.sort.Sort
import com.github.trading.core.port.model.sort.TradeSessionSort
import com.github.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyType
import com.github.trading.domain.entity.TradeSessionStatus
import com.github.trading.domain.model.CandleInterval
import com.github.trading.infra.adapter.income.web.rest.model.common.InstrumentDto
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class CreateTradeSessionRequestDto(
    @Schema(description = "Strategy configuration id") val strategyConfigurationId: UUID,
    @Schema(description = "Order lots quantity strategy type") val orderLotsQuantityStrategyType: OrderLotsQuantityStrategyType,
    @Schema(description = "Instrument") val instrument: InstrumentDto
)

data class SearchTradeSessionRequestDto(
    val id: UUID? = null,
    val ticker: String? = null,
    val instrumentId: String? = null,
    val status: TradeSessionStatus? = null,
    val candleInterval: CandleInterval? = null,
    val page: Page? = null,
    val sort: Sort<TradeSessionSort>? = null
)