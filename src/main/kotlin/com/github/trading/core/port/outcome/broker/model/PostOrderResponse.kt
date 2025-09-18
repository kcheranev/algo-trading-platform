package com.github.trading.core.port.outcome.broker.model

import java.math.BigDecimal

data class PostOrderResponse(
    val orderId: String,
    val status: PostOrderResponseStatus,
    val lotsRequested: Int,
    val lotsExecuted: Int,
    val totalPrice: BigDecimal,
    val executedPrice: BigDecimal,
    val executedCommission: BigDecimal
) {

    fun executed() = status == PostOrderResponseStatus.FILL || status == PostOrderResponseStatus.PARTIALLY_FILL

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