package ru.kcheranev.trading.core.port.outcome.broker.model

import java.math.BigDecimal

data class PostOrderResponse(
    val orderId: String,
    val status: PostOrderResponseStatus,
    val lotsRequested: Int,
    val lotsExecuted: Int,
    val totalPrice: BigDecimal,
    val executedCommission: BigDecimal
) {

    fun executed() = status == PostOrderResponseStatus.FILL || status == PostOrderResponseStatus.PARTIALLY_FILL

    override fun toString() =
        "[orderId=$orderId, status=$status, lotsRequested=$lotsRequested, lotsExecuted=$lotsExecuted, " +
                "totalPrice=$totalPrice, executedCommission=$executedCommission]"

}

enum class PostOrderResponseStatus {

    UNSPECIFIED,

    //Исполнена
    FILL,

    //Отклонена
    REJECTED,

    //Отменена пользователем
    CANCELLED,

    //Новая
    NEW,

    //Частично исполнена
    PARTIALLY_FILL,

    UNRECOGNIZED

}