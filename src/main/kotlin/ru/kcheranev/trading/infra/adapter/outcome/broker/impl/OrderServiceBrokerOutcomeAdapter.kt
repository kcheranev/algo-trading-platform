package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.kcheranev.trading.core.port.outcome.broker.OrderServiceBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.PostBestPriceBuyOrderCommand
import ru.kcheranev.trading.core.port.outcome.broker.PostBestPriceSellOrderCommand
import ru.kcheranev.trading.core.port.outcome.broker.model.PostOrderResponse
import ru.kcheranev.trading.infra.adapter.outcome.broker.brokerOutcomeAdapterMapper
import ru.kcheranev.trading.infra.adapter.outcome.broker.delegate.LoggingOrdersServiceDelegate
import ru.tinkoff.piapi.contract.v1.OrderDirection
import ru.tinkoff.piapi.contract.v1.OrderType
import ru.tinkoff.piapi.contract.v1.Quotation
import java.util.UUID

@Component
class OrderServiceBrokerOutcomeAdapter(
    private val loggingOrdersServiceDelegate: LoggingOrdersServiceDelegate,
    private val userServiceBrokerOutcomeAdapter: UserServiceBrokerOutcomeAdapter,
) : OrderServiceBrokerPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun postBestPriceBuyOrder(
        command: PostBestPriceBuyOrderCommand
    ): PostOrderResponse {
        log.info("Post buy best price order for the trade session ticker=${command.instrument.ticker}")
        return loggingOrdersServiceDelegate.postOrderSync(
            command.instrument.id,
            command.quantity.toLong(),
            Quotation.getDefaultInstance(),
            OrderDirection.ORDER_DIRECTION_BUY,
            userServiceBrokerOutcomeAdapter.getTradingAccountId(),
            OrderType.ORDER_TYPE_BESTPRICE,
            UUID.randomUUID().toString()
        ).let { brokerOutcomeAdapterMapper.map(it) }
    }

    override fun postBestPriceSellOrder(
        command: PostBestPriceSellOrderCommand
    ): PostOrderResponse {
        log.info("Post sell best price order for the trade session ticker=${command.instrument.ticker}")
        return loggingOrdersServiceDelegate.postOrderSync(
            command.instrument.id,
            command.quantity.toLong(),
            Quotation.getDefaultInstance(),
            OrderDirection.ORDER_DIRECTION_SELL,
            userServiceBrokerOutcomeAdapter.getTradingAccountId(),
            OrderType.ORDER_TYPE_BESTPRICE,
            UUID.randomUUID().toString()
        ).let { brokerOutcomeAdapterMapper.map(it) }
    }

}