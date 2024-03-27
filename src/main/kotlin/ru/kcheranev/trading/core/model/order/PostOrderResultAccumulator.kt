package ru.kcheranev.trading.core.model.order

import ru.kcheranev.trading.core.port.outcome.broker.model.PostOrderResponse
import java.math.BigDecimal

data class PostOrderResultAccumulator(
    val lotsRequested: Int,
    var lotsExecuted: Int = 0,
    var totalPrice: BigDecimal = BigDecimal.ZERO,
    var executedCommission: BigDecimal = BigDecimal.ZERO
) {

    fun accumulate(postOrderResponse: PostOrderResponse) {
        lotsExecuted += postOrderResponse.lotsExecuted
        totalPrice += postOrderResponse.totalPrice
        executedCommission += postOrderResponse.executedCommission
    }

    fun haveOrders() = lotsExecuted > 0

    fun completed() = lotsRequested == lotsExecuted

    fun remainLotsQuantity() = lotsRequested - lotsExecuted

}