package ru.kcheranev.trading.core.strategy.lotsquantity

import org.springframework.stereotype.Component
import ru.kcheranev.trading.domain.entity.TradeSession

sealed interface OrderLotsQuantityStrategy {

    val type: OrderLotsQuantityStrategyType

    fun getLotsQuantity(tradeSession: TradeSession): Int

}

enum class OrderLotsQuantityStrategyType {

    HARDCODED, DEPOSIT_DEPENDENT

}

@Component
class OrderLotsQuantityStrategyProvider(private val orderLotsQuantityStrategyList: List<OrderLotsQuantityStrategy>) {

    fun getOrderLotsQuantityStrategy(orderLotsQuantityStrategyType: OrderLotsQuantityStrategyType) =
        orderLotsQuantityStrategyList.first { it.type == orderLotsQuantityStrategyType }

}