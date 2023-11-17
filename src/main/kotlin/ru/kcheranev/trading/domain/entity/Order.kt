package ru.kcheranev.trading.domain.entity

import ru.kcheranev.trading.domain.AbstractEntity
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
) : AbstractEntity() {
}

data class OrderId(
    val value: Long
)

enum class TradeDirection {

    BUY, SELL

}