package com.github.trading.core.strategy.lotsquantity

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.github.trading.core.error.AppError
import com.github.trading.core.error.DomainError
import com.github.trading.core.port.outcome.broker.GetMaxLotsCommand
import com.github.trading.core.port.outcome.broker.OrderServiceBrokerPort
import com.github.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyType.HARDCODED
import com.github.trading.domain.entity.TradeSession
import org.springframework.stereotype.Component
import kotlin.math.min

const val LOTS_QUANTITY_STRATEGY_PARAMETER_NAME = "lotsQuantity"

@Component
class HardcodedOrderLotsQuantityStrategy(
    private val orderServiceBrokerPort: OrderServiceBrokerPort
) : OrderLotsQuantityStrategy {

    override val type = HARDCODED

    override fun getLotsQuantity(tradeSession: TradeSession): Either<AppError, Int> =
        either {
            val hardcodedLotsQuantity = tradeSession.strategyParameters.getAsInt(LOTS_QUANTITY_STRATEGY_PARAMETER_NAME).bind()
            val maxAvailableLots = orderServiceBrokerPort.getMaxLots(GetMaxLotsCommand(tradeSession.instrument)).bind()
            val resultLotsQuantity =
                if (tradeSession.isMargin()) {
                    min(hardcodedLotsQuantity, maxAvailableLots.sellMarginLimits.sellMaxLots)
                } else {
                    min(hardcodedLotsQuantity, maxAvailableLots.buyMarginLimits.buyMaxMarketLots)
                }
            ensure(resultLotsQuantity > 0) { DomainError.NotEnoughMoneyOnDepositError }
            resultLotsQuantity
        }

}