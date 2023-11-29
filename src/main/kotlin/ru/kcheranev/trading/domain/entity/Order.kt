package ru.kcheranev.trading.domain.entity

import org.springframework.data.domain.AbstractAggregateRoot
import java.math.BigDecimal
import java.time.LocalDate

data class Order(
    val id: OrderId,
    val ticker: String,
    val date: LocalDate,
    val quantity: Int,
    val price: BigDecimal,
    val direction: TradeDirection,
    val tradeSessionId: TradeSessionId
) : AbstractAggregateRoot<Order>() {
}

data class OrderId(
    val value: Long
)

enum class TradeDirection {

    BUY, SELL

}