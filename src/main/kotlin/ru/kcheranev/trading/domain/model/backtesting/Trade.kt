package ru.kcheranev.trading.domain.model.backtesting

import ru.kcheranev.trading.common.date.max
import ru.kcheranev.trading.common.date.min
import ru.kcheranev.trading.domain.model.TradeDirection
import java.math.BigDecimal

data class Trade(
    val entry: Order,
    val exit: Order?
) {

    val netProfit: BigDecimal =
        if (exit == null) {
            BigDecimal.ZERO
        } else {
            when (entry.direction) {
                TradeDirection.BUY -> max(exit.netPrice - entry.netPrice, BigDecimal.ZERO)
                TradeDirection.SELL -> max(entry.netPrice - exit.netPrice, BigDecimal.ZERO)
            }
        }


    val grossProfit: BigDecimal =
        if (exit == null) {
            BigDecimal.ZERO
        } else {
            when (entry.direction) {
                TradeDirection.BUY -> max(exit.grossPrice - entry.grossPrice, BigDecimal.ZERO)
                TradeDirection.SELL -> max(entry.grossPrice - exit.grossPrice, BigDecimal.ZERO)
            }
        }

    val netLoss: BigDecimal =
        if (exit == null) {
            BigDecimal.ZERO
        } else {
            when (entry.direction) {
                TradeDirection.BUY -> min(exit.netPrice - entry.netPrice, BigDecimal.ZERO)
                TradeDirection.SELL -> min(entry.netPrice - exit.netPrice, BigDecimal.ZERO)
            }
        }


    val grossLoss: BigDecimal =
        if (exit == null) {
            BigDecimal.ZERO
        } else {
            when (entry.direction) {
                TradeDirection.BUY -> min(exit.grossPrice - entry.grossPrice, BigDecimal.ZERO)
                TradeDirection.SELL -> min(entry.grossPrice - exit.grossPrice, BigDecimal.ZERO)
            }
        }

    val grossValue: BigDecimal = if (exit == null) BigDecimal.ZERO else grossProfit + grossLoss

    val netValue: BigDecimal = if (exit == null) BigDecimal.ZERO else netProfit + netLoss

    fun profitPosition() =
        if (exit == null) {
            false
        } else {
            when (entry.direction) {
                TradeDirection.BUY -> exit.netPrice > entry.netPrice
                TradeDirection.SELL -> exit.netPrice < entry.netPrice
            }
        }

    fun losingPosition() =
        if (exit == null) {
            false
        } else {
            when (entry.direction) {
                TradeDirection.BUY -> exit.netPrice < entry.netPrice
                TradeDirection.SELL -> exit.netPrice > entry.netPrice
            }
        }

    fun closed() = exit != null

}