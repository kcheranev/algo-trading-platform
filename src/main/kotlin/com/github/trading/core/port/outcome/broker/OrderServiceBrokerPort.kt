package com.github.trading.core.port.outcome.broker

import arrow.core.Either
import com.github.trading.core.error.BrokerIntegrationError
import com.github.trading.core.port.outcome.broker.model.PostOrderResponse

interface OrderServiceBrokerPort {

    fun postBestPriceBuyOrder(command: PostBestPriceBuyOrderCommand): Either<BrokerIntegrationError, PostOrderResponse>

    fun postBestPriceSellOrder(command: PostBestPriceSellOrderCommand): Either<BrokerIntegrationError, PostOrderResponse>

}