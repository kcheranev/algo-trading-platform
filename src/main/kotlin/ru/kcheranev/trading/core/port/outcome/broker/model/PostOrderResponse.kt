package ru.kcheranev.trading.core.port.outcome.broker.model

data class PostOrderResponse(
    val orderId: String,
    val status: PostOrderStatus,
    val lotsRequested: Long,
    val lotsExecuted: Long
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
    PARTIALLYFILL

}