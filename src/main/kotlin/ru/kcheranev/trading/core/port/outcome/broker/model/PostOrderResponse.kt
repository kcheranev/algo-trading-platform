package ru.kcheranev.trading.core.port.outcome.broker.model

import java.math.BigDecimal

data class PostOrderResponse(
    val orderId: String,
    val status: PostOrderStatus,
    val lotsRequested: Long,
    val lotsExecuted: Long,
    val totalPrice: BigDecimal,
    val executedCommission: BigDecimal
)

enum class PostOrderStatus {

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