package com.github.trading.core.strategy.lotsquantity

import arrow.core.Either
import com.github.trading.core.error.AppError
import com.github.trading.domain.entity.TradeSession
import org.springframework.stereotype.Component

sealed interface OrderLotsQuantityStrategy {

    val type: OrderLotsQuantityStrategyType

    fun getLotsQuantity(tradeSession: TradeSession): Either<AppError, Int>

}

enum class OrderLotsQuantityStrategyType {

    HARDCODED, DEPOSIT_PERCENT

}

@Component
class OrderLotsQuantityStrategyProvider(private val orderLotsQuantityStrategyList: List<OrderLotsQuantityStrategy>) {

    fun getOrderLotsQuantityStrategy(orderLotsQuantityStrategyType: OrderLotsQuantityStrategyType) =
        orderLotsQuantityStrategyList.first { it.type == orderLotsQuantityStrategyType }

}