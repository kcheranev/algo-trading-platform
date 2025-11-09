package com.github.trading.infra.adapter.outcome.broker.logging

import org.slf4j.LoggerFactory
import ru.tinkoff.piapi.contract.v1.OrderDirection
import ru.tinkoff.piapi.contract.v1.OrderType
import ru.tinkoff.piapi.contract.v1.OrdersServiceGrpc.OrdersServiceBlockingStub
import ru.tinkoff.piapi.contract.v1.PostOrderRequest
import ru.tinkoff.piapi.contract.v1.PostOrderResponse
import ru.tinkoff.piapi.contract.v1.Quotation
import ru.ttech.piapi.core.connector.SyncStubWrapper
import ru.ttech.piapi.core.helpers.NumberMapper.moneyValueToBigDecimal

class LoggingOrdersServiceDecorator(private val brokerOrdersServiceWrapper: SyncStubWrapper<OrdersServiceBlockingStub>) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun postOrderSync(
        instrumentId: String,
        quantity: Long,
        price: Quotation,
        direction: OrderDirection,
        accountId: String,
        orderType: OrderType,
        orderId: String
    ): PostOrderResponse {
        log.info(
            """
            Outgoing broker postOrder request with parameters
            instrumentId = $instrumentId
            quantity = $quantity
            price = $price
            direction = $direction
            accountId = $accountId
            type = $orderType
            orderId = $orderId
        """.trimIndent()
        )
        val postOrderResponse = brokerOrdersServiceWrapper.callSyncMethod { stub ->
            stub.postOrder(
                PostOrderRequest.newBuilder()
                    .setInstrumentId(instrumentId)
                    .setQuantity(quantity)
                    .setPrice(price)
                    .setDirection(direction)
                    .setAccountId(accountId)
                    .setOrderType(orderType)
                    .setOrderId(orderId)
                    .build()
            )
        }
        log.info(
            """
            Outgoing broker postOrder response with result
            orderId = ${postOrderResponse.orderId}
            executionReportStatus = ${postOrderResponse.executionReportStatus}
            lotsRequested = ${postOrderResponse.lotsRequested}
            lotsExecuted = ${postOrderResponse.lotsExecuted}
            initialOrderPrice = ${moneyValueToBigDecimal(postOrderResponse.initialOrderPrice)}
            executedOrderPrice = ${moneyValueToBigDecimal(postOrderResponse.executedOrderPrice)}
            totalOrderAmount = ${moneyValueToBigDecimal(postOrderResponse.totalOrderAmount)}
            initialCommission = ${moneyValueToBigDecimal(postOrderResponse.initialCommission)}
            executedCommission = ${moneyValueToBigDecimal(postOrderResponse.executedCommission)}
            direction = ${postOrderResponse.direction}
            initialSecurityPrice = ${moneyValueToBigDecimal(postOrderResponse.initialSecurityPrice)}
            orderType = ${postOrderResponse.orderType}
            message = ${postOrderResponse.message}
            instrumentUid = ${postOrderResponse.instrumentUid}
        """.trimIndent()
        )
        return postOrderResponse
    }

}