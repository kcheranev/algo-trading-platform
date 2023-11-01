package ru.kcheranev.trading.domain.entity

import java.math.BigDecimal
import java.time.LocalDate

data class Order(
    val id: OrderId,
    val ticker: String,
    val data: LocalDate,
    val quantity: Int,
    val price: BigDecimal,
    val direction: TradeDirection
) {
}

data class OrderId(
    val value: Int
)

enum class TradeDirection {

    BUY, SELL

}