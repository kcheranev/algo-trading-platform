package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import org.springframework.stereotype.Component
import ru.kcheranev.trading.common.LoggerDelegate
import ru.kcheranev.trading.core.port.outcome.broker.OrderServiceBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.PostBestPriceBuyOrderCommand
import ru.kcheranev.trading.core.port.outcome.broker.PostBestPriceSellOrderCommand
import ru.kcheranev.trading.core.port.outcome.broker.model.PostOrderResponse
import ru.kcheranev.trading.infra.adapter.outcome.broker.brokerOutcomeAdapterMapper
import ru.kcheranev.trading.infra.config.BrokerApi
import ru.tinkoff.piapi.contract.v1.OrderDirection
import ru.tinkoff.piapi.contract.v1.OrderType
import ru.tinkoff.piapi.contract.v1.Quotation
import java.util.UUID
import java.util.concurrent.CompletableFuture

@Component
class OrderServiceBrokerOutcomeAdapter(
    brokerApi: BrokerApi,
    private val userServiceBrokerOutcomeAdapter: UserServiceBrokerOutcomeAdapter,
) : OrderServiceBrokerPort {

    private val orderService = brokerApi.orderService

    override fun postBestPriceBuyOrder(
        command: PostBestPriceBuyOrderCommand
    ): CompletableFuture<PostOrderResponse> {
        logger.info("Post buy best price order for the ${command.ticker}")
        return orderService.postOrder(
            command.instrumentId,
            command.quantity,
            Quotation.getDefaultInstance(),
            OrderDirection.ORDER_DIRECTION_BUY,
            userServiceBrokerOutcomeAdapter.getTradingAccountId(),
            OrderType.ORDER_TYPE_BESTPRICE,
            UUID.randomUUID().toString()
        ).thenApply { brokerOutcomeAdapterMapper.map(it) }
    }

    override fun postBestPriceSellOrder(
        command: PostBestPriceSellOrderCommand
    ): CompletableFuture<PostOrderResponse> {
        logger.info("Post sell best price order for the ${command.ticker}")
        return orderService.postOrder(
            command.instrumentId,
            command.quantity,
            Quotation.getDefaultInstance(),
            OrderDirection.ORDER_DIRECTION_SELL,
            userServiceBrokerOutcomeAdapter.getTradingAccountId(),
            OrderType.ORDER_TYPE_BESTPRICE,
            UUID.randomUUID().toString()
        ).thenApply { brokerOutcomeAdapterMapper.map(it) }
    }

    companion object {

        private val logger by LoggerDelegate()

    }

}