package com.github.trading.infra.adapter.income.web.ui.model.request

import com.github.trading.core.port.model.Page
import com.github.trading.core.port.model.sort.Sort
import com.github.trading.core.port.model.sort.TradeSessionSort
import com.github.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyType
import com.github.trading.domain.entity.TradeSessionStatus
import com.github.trading.domain.model.CandleInterval
import java.util.UUID

data class CreateTradeSessionRequestUiDto(
    val strategyConfigurationId: UUID,
    val orderLotsQuantityStrategyType: OrderLotsQuantityStrategyType,
    val instrument: InstrumentRequestUiDto
)

data class SearchTradeSessionRequestUiDto(
    val id: UUID? = null,
    val ticker: String? = null,
    val instrumentId: String? = null,
    val status: TradeSessionStatus? = null,
    val candleInterval: CandleInterval? = null,
    val page: Page? = null,
    val sort: Sort<TradeSessionSort>? = null
)