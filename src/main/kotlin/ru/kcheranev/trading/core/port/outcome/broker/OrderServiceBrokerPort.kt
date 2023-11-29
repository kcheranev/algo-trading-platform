package ru.kcheranev.trading.core.port.outcome.broker

import ru.kcheranev.trading.core.port.outcome.broker.model.PostOrderResponse
import java.util.concurrent.CompletableFuture

interface OrderServiceBrokerPort {

    fun postBestPriceBuyOrder(command: PostBestPriceBuyOrderCommand): CompletableFuture<PostOrderResponse>

    fun postBestPriceBuyOrderSync(command: PostBestPriceBuyOrderCommand): PostOrderResponse =
        postBestPriceBuyOrder(command).get()

    fun postBestPriceSellOrder(command: PostBestPriceSellOrderCommand): CompletableFuture<PostOrderResponse>

    fun postBestPriceSellOrderSync(command: PostBestPriceSellOrderCommand): PostOrderResponse =
        postBestPriceSellOrder(command).get()

}