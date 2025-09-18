package com.github.trading.infra.adapter.income.web.rest.model.response

import com.github.trading.domain.model.TradeDirection
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class TradeOrderDto(
    val id: UUID,
    val ticker: String,
    val instrumentId: String,
    val date: LocalDateTime,
    val lotsQuantity: Int,
    val totalPrice: BigDecimal,
    val executedCommission: BigDecimal,
    val direction: TradeDirection,
    val tradeSessionId: UUID
)

data class TradeOrderSearchResponseDto(
    val tradeOrders: List<TradeOrderDto>
)