package ru.kcheranev.trading.core.strategy.lotsquantity

import org.springframework.stereotype.Component
import ru.kcheranev.trading.domain.entity.TradeSession

@Component
class DepositDependentOrderLotsQuantityStrategy : OrderLotsQuantityStrategy {

    override val type = OrderLotsQuantityStrategyType.DEPOSIT_DEPENDENT

    override fun getLotsQuantity(tradeSession: TradeSession): Int {
        TODO("Not yet implemented")
    }

}