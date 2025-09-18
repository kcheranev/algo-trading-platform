package com.github.trading.core.port.outcome.persistence.tradeorder

import com.github.trading.core.port.model.ComparedField
import com.github.trading.core.port.model.Page
import com.github.trading.core.port.model.sort.Sort
import com.github.trading.core.port.model.sort.TradeOrderSort
import com.github.trading.domain.entity.TradeOrder
import com.github.trading.domain.entity.TradeOrderId
import com.github.trading.domain.entity.TradeSessionId
import com.github.trading.domain.model.TradeDirection
import java.math.BigDecimal
import java.time.LocalDateTime

data class InsertTradeOrderCommand(
    val tradeOrder: TradeOrder
)

data class GetTradeOrderCommand(
    val tradeOrderId: TradeOrderId
)

data class SearchTradeOrderCommand(
    val id: TradeOrderId?,
    val ticker: String?,
    val instrumentId: String?,
    val date: ComparedField<LocalDateTime>?,
    val lotsQuantity: ComparedField<Int>?,
    val totalPrice: ComparedField<BigDecimal>?,
    val direction: TradeDirection?,
    val tradeSessionId: TradeSessionId?,
    val page: Page?,
    val sort: Sort<TradeOrderSort>?
)