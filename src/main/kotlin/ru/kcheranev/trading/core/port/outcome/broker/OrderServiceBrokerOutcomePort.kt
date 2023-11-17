package ru.kcheranev.trading.core.port.outcome.broker

import ru.kcheranev.trading.core.port.outcome.broker.model.PostOrderResponse
import java.util.concurrent.CompletableFuture

interface OrderServiceBrokerOutcomePort {

    fun postBestPriceBuyOrder(command: PostBestPriceBuyOrderBrokerOutcomeCommand): CompletableFuture<PostOrderResponse>

    fun postBestPriceSellOrder(command: PostBestPriceSellOrderBrokerOutcomeCommand): CompletableFuture<PostOrderResponse>

}