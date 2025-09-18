package com.github.trading.domain.entity

import com.github.trading.common.date.DateSupplier
import com.github.trading.domain.model.TradeDirection
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class TradeOrder(
    val id: TradeOrderId,
    val ticker: String,
    val instrumentId: String,
    val date: LocalDateTime,
    val lotsQuantity: Int,
    val totalPrice: BigDecimal,
    val executedCommission: BigDecimal,
    val direction: TradeDirection,
    val tradeSessionId: TradeSessionId
) : AbstractAggregateRoot() {

    companion object {

        fun create(
            ticker: String,
            instrumentId: String,
            lotsQuantity: Int,
            totalPrice: BigDecimal,
            executedCommission: BigDecimal,
            direction: TradeDirection,
            tradeSessionId: TradeSessionId,
        ) = TradeOrder(
            id = TradeOrderId.init(),
            ticker = ticker,
            instrumentId = instrumentId,
            date = DateSupplier.currentDateTime(),
            lotsQuantity = lotsQuantity,
            totalPrice = totalPrice,
            executedCommission = executedCommission,
            direction = direction,
            tradeSessionId = tradeSessionId
        )

    }

}

data class TradeOrderId(
    val value: UUID
) {

    override fun toString() = value.toString()

    companion object {

        fun init() = TradeOrderId(UUID.randomUUID())

    }

}