package ru.kcheranev.trading.domain.entity

import ru.kcheranev.trading.common.DateSupplier
import java.math.BigDecimal
import java.time.LocalDateTime

data class TradeOrder(
    val id: TradeOrderId?,
    val ticker: String,
    val instrumentId: String,
    val date: LocalDateTime,
    val lotsQuantity: Int,
    val price: BigDecimal,
    val direction: TradeDirection,
    val tradeSessionId: TradeSessionId
) : AbstractAggregateRoot() {

    companion object {

        fun create(
            ticker: String,
            instrumentId: String,
            lotsQuantity: Int,
            price: BigDecimal,
            direction: TradeDirection,
            tradeSessionId: TradeSessionId,
            dateSupplier: DateSupplier
        ): TradeOrder =
            TradeOrder(
                id = null,
                ticker = ticker,
                instrumentId = instrumentId,
                date = dateSupplier.currentDate(),
                lotsQuantity = lotsQuantity,
                price = price,
                direction = direction,
                tradeSessionId = tradeSessionId
            )

    }

}

data class TradeOrderId(
    val value: Long
)

enum class TradeDirection {

    BUY, SELL

}

enum class TradeOrderSort : SortField {

    TICKER, DATE, PRICE, DIRECTION

}