package ru.kcheranev.trading.infra.adapter.outcome.broker.impl

import org.springframework.stereotype.Component
import ru.kcheranev.trading.core.port.outcome.broker.OrderServiceBrokerPort
import ru.kcheranev.trading.core.port.outcome.broker.PostBestPriceBuyOrderCommand
import ru.kcheranev.trading.core.port.outcome.broker.PostBestPriceSellOrderCommand
import ru.kcheranev.trading.core.port.outcome.notification.NotificationPort
import ru.kcheranev.trading.core.port.outcome.notification.SendNotificationCommand
import ru.kcheranev.trading.infra.adapter.outcome.broker.brokerOutcomeAdapterMapper
import ru.kcheranev.trading.infra.adapter.outcome.broker.logging.LoggingOrdersServiceDecorator
import ru.tinkoff.piapi.contract.v1.OrderDirection
import ru.tinkoff.piapi.contract.v1.OrderType
import ru.tinkoff.piapi.contract.v1.Quotation
import java.util.UUID

@Component
class OrderServiceBrokerOutcomeAdapter(
    private val loggingOrdersServiceDecorator: LoggingOrdersServiceDecorator,
    private val userServiceBrokerOutcomeAdapter: UserServiceBrokerOutcomeAdapter,
    private val notificationPort: NotificationPort
) : OrderServiceBrokerPort {

    override fun postBestPriceBuyOrder(
        command: PostBestPriceBuyOrderCommand
    ) = try {
        notificationPort.sendNotification(
            SendNotificationCommand(
                "Post best price buy order: ticker=${command.instrument.ticker}"
            )
        )
        loggingOrdersServiceDecorator.postOrderSync(
            command.instrument.id,
            command.quantity.toLong(),
            Quotation.getDefaultInstance(),
            OrderDirection.ORDER_DIRECTION_BUY,
            userServiceBrokerOutcomeAdapter.getTradingAccountId(),
            OrderType.ORDER_TYPE_BESTPRICE,
            UUID.randomUUID().toString()
        ).let { brokerOutcomeAdapterMapper.map(it) }
    } catch (ex: Exception) {
        notificationPort.sendNotification(
            SendNotificationCommand(
                "An error has been occurred while post best price buy order: ticker=${command.instrument.ticker}"
            )
        )
        throw ex
    }

    override fun postBestPriceSellOrder(
        command: PostBestPriceSellOrderCommand
    ) = try {
        notificationPort.sendNotification(
            SendNotificationCommand(
                "Post best price sell order: ticker=${command.instrument.ticker}"
            )
        )
        loggingOrdersServiceDecorator.postOrderSync(
            command.instrument.id,
            command.quantity.toLong(),
            Quotation.getDefaultInstance(),
            OrderDirection.ORDER_DIRECTION_SELL,
            userServiceBrokerOutcomeAdapter.getTradingAccountId(),
            OrderType.ORDER_TYPE_BESTPRICE,
            UUID.randomUUID().toString()
        ).let { brokerOutcomeAdapterMapper.map(it) }
    } catch (ex: Exception) {
        notificationPort.sendNotification(
            SendNotificationCommand(
                "An error has been occurred while post best price sell order: ticker=${command.instrument.ticker}"
            )
        )
        throw ex
    }

}