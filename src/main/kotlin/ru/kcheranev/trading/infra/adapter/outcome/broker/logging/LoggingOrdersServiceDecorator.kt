package ru.kcheranev.trading.infra.adapter.outcome.broker.logging

import org.slf4j.LoggerFactory
import ru.tinkoff.piapi.contract.v1.OrderDirection
import ru.tinkoff.piapi.contract.v1.OrderType
import ru.tinkoff.piapi.contract.v1.PostOrderResponse
import ru.tinkoff.piapi.contract.v1.Quotation
import ru.tinkoff.piapi.core.OrdersService
import ru.tinkoff.piapi.core.utils.MapperUtils.moneyValueToBigDecimal

class LoggingOrdersServiceDecorator(private val ordersService: OrdersService) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun postOrderSync(
        instrumentId: String,
        quantity: Long,
        price: Quotation,
        direction: OrderDirection,
        accountId: String,
        type: OrderType,
        orderId: String
    ): PostOrderResponse {
        log.info(
            """
            Outgoing broker postOrderSync request with parameters
            instrumentId = $instrumentId
            quantity = $quantity
            price = $price
            direction = $direction
            accountId = $accountId
            type = $type
            orderId = $orderId
        """.trimIndent()
        )
        val postOrderResponse = ordersService.postOrderSync(
            instrumentId,
            quantity,
            price,
            direction,
            accountId,
            type,
            orderId
        )
        log.info(
            """
            Outgoing broker postOrderSync response with result
            orderId = ${postOrderResponse.orderId}
            executionReportStatus = ${postOrderResponse.executionReportStatus}
            lotsRequested = ${postOrderResponse.lotsRequested}
            lotsExecuted = ${postOrderResponse.lotsExecuted}
            initialOrderPrice = ${moneyValueToBigDecimal(postOrderResponse.initialOrderPrice)}
            executedOrderPrice = ${moneyValueToBigDecimal(postOrderResponse.executedOrderPrice)}
            totalOrderAmount = ${moneyValueToBigDecimal(postOrderResponse.totalOrderAmount)}
            initialCommission = ${moneyValueToBigDecimal(postOrderResponse.initialCommission)}
            executedCommission = ${moneyValueToBigDecimal(postOrderResponse.executedCommission)}
            figi = ${postOrderResponse.figi}
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