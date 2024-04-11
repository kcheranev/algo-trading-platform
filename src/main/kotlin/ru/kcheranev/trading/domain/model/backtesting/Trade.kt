package ru.kcheranev.trading.domain.model.backtesting

import ru.kcheranev.trading.domain.model.TradeDirection

data class Trade(
    val entry: Order,
    val exit: Order
) {

    val netProfit =
        if (entry.direction == TradeDirection.BUY) {
            exit.netPrice - entry.netPrice
        } else {
            entry.netPrice - exit.netPrice
        }

    val grossProfit =
        if (entry.direction == TradeDirection.BUY) {
            exit.grossPrice - entry.grossPrice
        } else {
            entry.grossPrice - exit.grossPrice
        }

}