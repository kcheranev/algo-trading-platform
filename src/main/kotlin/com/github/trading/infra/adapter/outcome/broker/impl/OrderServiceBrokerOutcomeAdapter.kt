package com.github.trading.infra.adapter.outcome.broker.impl

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.raise.either
import com.github.trading.common.format
import com.github.trading.core.error.BestPriceBuyOrderExecutionError
import com.github.trading.core.error.BestPriceSellOrderExecutionError
import com.github.trading.core.error.BrokerIntegrationError
import com.github.trading.core.port.outcome.broker.OrderServiceBrokerPort
import com.github.trading.core.port.outcome.broker.PostBestPriceBuyOrderCommand
import com.github.trading.core.port.outcome.broker.PostBestPriceSellOrderCommand
import com.github.trading.core.port.outcome.broker.model.PostOrderResponse
import com.github.trading.core.port.outcome.notification.NotificationPort
import com.github.trading.core.port.outcome.notification.SendNotificationCommand
import com.github.trading.infra.adapter.outcome.broker.brokerOutcomeAdapterMapper
import com.github.trading.infra.adapter.outcome.broker.logging.LoggingOrdersServiceDecorator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
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

    private val log = LoggerFactory.getLogger(javaClass)

    override fun postBestPriceBuyOrder(command: PostBestPriceBuyOrderCommand): Either<BrokerIntegrationError, PostOrderResponse> =
        either {
            catch {
                loggingOrdersServiceDecorator.postOrderSync(
                    command.instrument.id,
                    command.quantity.toLong(),
                    Quotation.getDefaultInstance(),
                    OrderDirection.ORDER_DIRECTION_BUY,
                    userServiceBrokerOutcomeAdapter.getTradingAccountId().bind(),
                    OrderType.ORDER_TYPE_BESTPRICE,
                    UUID.randomUUID().toString()
                )
            }.onLeft { ex ->
                val errorMessage =
                    "An error has been occurred while sending best price buy order: ticker=${command.instrument.ticker}"
                log.error(errorMessage, ex)
                notificationPort.sendNotification(SendNotificationCommand(errorMessage))
            }.mapLeft { BestPriceBuyOrderExecutionError }
                .bind()
        }.map(brokerOutcomeAdapterMapper::map)
            .onRight { postOrderResponse ->
                notificationPort.sendNotification(
                    SendNotificationCommand(
                        """
                        Buy order executed:
                        ticker=${command.instrument.ticker}
                        status=${postOrderResponse.status}
                        lotsRequested=${postOrderResponse.lotsRequested}
                        lotsExecuted=${postOrderResponse.lotsExecuted}
                        executedPrice=${postOrderResponse.executedPrice.format()}
                        totalPrice=${postOrderResponse.totalPrice.format()}
                        executedCommission=${postOrderResponse.executedCommission.format()}
                    """.trimIndent()
                    )
                )
            }

    override fun postBestPriceSellOrder(command: PostBestPriceSellOrderCommand): Either<BrokerIntegrationError, PostOrderResponse> =
        either {
            catch {
                loggingOrdersServiceDecorator.postOrderSync(
                    command.instrument.id,
                    command.quantity.toLong(),
                    Quotation.getDefaultInstance(),
                    OrderDirection.ORDER_DIRECTION_SELL,
                    userServiceBrokerOutcomeAdapter.getTradingAccountId().bind(),
                    OrderType.ORDER_TYPE_BESTPRICE,
                    UUID.randomUUID().toString()
                )
            }.onLeft { ex ->
                val errorMessage =
                    "An error has been occurred while sending best price sell order: ticker=${command.instrument.ticker}"
                log.error(errorMessage, ex)
                notificationPort.sendNotification(SendNotificationCommand(errorMessage))
            }.mapLeft { BestPriceSellOrderExecutionError }
                .bind()
        }.map(brokerOutcomeAdapterMapper::map)
            .onRight { postOrderResponse ->
                notificationPort.sendNotification(
                    SendNotificationCommand(
                        """
                        Sell order executed:
                        ticker=${command.instrument.ticker}
                        status=${postOrderResponse.status}
                        lotsRequested=${postOrderResponse.lotsRequested}
                        lotsExecuted=${postOrderResponse.lotsExecuted}
                        executedPrice=${postOrderResponse.executedPrice.format()}
                        totalPrice=${postOrderResponse.totalPrice.format()}
                        executedCommission=${postOrderResponse.executedCommission.format()}
                    """.trimIndent()
                    )
                )
            }

}