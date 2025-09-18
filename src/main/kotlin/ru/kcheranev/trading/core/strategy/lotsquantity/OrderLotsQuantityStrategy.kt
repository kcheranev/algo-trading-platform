package ru.kcheranev.trading.core.strategy.lotsquantity

import arrow.core.Either
import org.springframework.stereotype.Component
import ru.kcheranev.trading.core.error.AppError
import ru.kcheranev.trading.domain.entity.TradeSession

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