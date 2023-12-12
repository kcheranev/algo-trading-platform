package ru.kcheranev.trading.domain.entity

import org.springframework.data.domain.AbstractAggregateRoot
import java.math.BigDecimal
import java.time.LocalDateTime

data class Order(
    val id: OrderId?,
    val ticker: String,
    val instrumentId: String,
    val date: LocalDateTime,
    val lotsQuantity: Int,
    val price: BigDecimal,
    val direction: OrderDirection,
    val tradeSessionId: TradeSessionId
) : AbstractAggregateRoot<Order>() {
}

data class OrderId(
    val value: Long
)

enum class OrderDirection {

    BUY, SELL

}

enum class OrderSort : SortField {
    TICKER, DATE, PRICE, DIRECTION
}