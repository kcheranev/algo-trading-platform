package ru.kcheranev.trading.domain.model.backtesting

import ru.kcheranev.trading.domain.model.TradeDirection

data class Trade(
    val entry: Order,
    val exit: Order?
) {

    val netProfit =
        exit?.let {
            when (entry.direction) {
                TradeDirection.BUY -> exit.netPrice - entry.netPrice
                TradeDirection.SELL -> entry.netPrice - exit.netPrice
            }
        }

    val grossProfit =
        exit?.let {
            when (entry.direction) {
                TradeDirection.BUY -> exit.grossPrice - entry.grossPrice
                TradeDirection.SELL -> entry.grossPrice - exit.grossPrice
            }
        }

}