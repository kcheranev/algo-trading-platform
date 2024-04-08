package ru.kcheranev.trading.domain.model.backtesting

import ru.kcheranev.trading.domain.model.TradeDirection

data class Trade(
    val entry: Order,
    val exit: Order
) {

    val profit =
        if (entry.direction == TradeDirection.BUY) {
            exit.price - entry.price
        } else {
            entry.price - exit.price
        }

}