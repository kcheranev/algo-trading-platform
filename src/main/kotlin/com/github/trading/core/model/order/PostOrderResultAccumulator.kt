package com.github.trading.core.model.order

import com.github.trading.core.port.outcome.broker.model.PostOrderResponse
import java.math.BigDecimal
import java.math.RoundingMode

private const val POST_ORDER_ACCUMULATOR_SCALE = 4

data class PostOrderResultAccumulator(
    val lotsRequested: Int,
    var lotsExecuted: Int = 0,
    var averagePrice: BigDecimal = BigDecimal.ZERO,
    var totalPrice: BigDecimal = BigDecimal.ZERO,
    var executedCommission: BigDecimal = BigDecimal.ZERO
) {

    fun accumulate(postOrderResponse: PostOrderResponse) {
        averagePrice =
            if (averagePrice == BigDecimal.ZERO) {
                postOrderResponse.executedPrice
            } else {
                (postOrderResponse.executedPrice * postOrderResponse.lotsExecuted.toBigDecimal() +
                        averagePrice * lotsExecuted.toBigDecimal())
                    .divide(
                        (postOrderResponse.lotsExecuted + lotsExecuted).toBigDecimal(),
                        POST_ORDER_ACCUMULATOR_SCALE,
                        RoundingMode.HALF_UP
                    )
            }
        lotsExecuted += postOrderResponse.lotsExecuted
        totalPrice += postOrderResponse.totalPrice
        executedCommission += postOrderResponse.executedCommission
    }

    fun haveOrders() = lotsExecuted > 0

    fun completed() = lotsRequested == lotsExecuted

    fun remainLotsQuantity() = lotsRequested - lotsExecuted

}