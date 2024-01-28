package ru.kcheranev.trading.core.port.outcome.broker

import ru.kcheranev.trading.core.port.outcome.broker.model.PostOrderResponse

interface OrderServiceBrokerPort {

    fun postBestPriceBuyOrder(command: PostBestPriceBuyOrderCommand): PostOrderResponse

    fun postBestPriceSellOrder(command: PostBestPriceSellOrderCommand): PostOrderResponse

}