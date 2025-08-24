package ru.kcheranev.trading.core.strategy.lotsquantity

import org.springframework.stereotype.Component
import ru.kcheranev.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyType.HARDCODED
import ru.kcheranev.trading.domain.entity.TradeSession

const val LOTS_QUANTITY_STRATEGY_PARAMETER_NAME = "lotsQuantity"

@Component
class HardcodedOrderLotsQuantityStrategy : OrderLotsQuantityStrategy {

    override val type = HARDCODED

    override fun getLotsQuantity(tradeSession: TradeSession) =
        tradeSession.strategyParameters.getAsInt(LOTS_QUANTITY_STRATEGY_PARAMETER_NAME)

}