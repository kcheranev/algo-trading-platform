package ru.kcheranev.trading.test.stub.grpc

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import io.kotest.matchers.collections.shouldHaveSize
import org.skyscreamer.jsonassert.Customization
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.comparator.CustomComparator
import org.wiremock.grpc.dsl.WireMockGrpc
import org.wiremock.grpc.dsl.WireMockGrpc.json
import org.wiremock.grpc.dsl.WireMockGrpcService
import ru.kcheranev.trading.test.stub.AbstractGrpcStub
import ru.tinkoff.piapi.contract.v1.OrderDirection
import ru.tinkoff.piapi.contract.v1.OrdersServiceGrpc


class OrdersBrokerGrpcStub(
    testName: String,
    private val grpcWireMockServer: WireMockServer
) : AbstractGrpcStub(testName) {

    private val ordersService =
        WireMockGrpcService(
            WireMock(grpcWireMockServer.port()),
            OrdersServiceGrpc.SERVICE_NAME
        )

    fun stubForPostBuyOrder(fileName: String) {
        stubForPostOrder(fileName, OrderDirection.ORDER_DIRECTION_BUY.name)
    }

    fun stubForPostSellOrder(fileName: String) {
        stubForPostOrder(fileName, OrderDirection.ORDER_DIRECTION_SELL.name)
    }

    private fun stubForPostOrder(responseFileName: String, orderDirection: String) {
        ordersService.stubFor(
            WireMockGrpc.method("PostOrder")
                .withRequestMessage(matchingJsonPath("$.direction", equalTo(orderDirection)))
                .willReturn(json(grpcResponse(responseFileName)))
        )
    }

    fun verifyForPostBuyOrder(fileName: String) {
        verifyForPostOrder(fileName, OrderDirection.ORDER_DIRECTION_BUY.name)
    }

    fun verifyForPostSellOrder(fileName: String) {
        verifyForPostOrder(fileName, OrderDirection.ORDER_DIRECTION_SELL.name)
    }

    private fun verifyForPostOrder(fileName: String, orderDirection: String) {
        val requests =
            grpcWireMockServer.findAll(
                postRequestedFor(urlMatching("/${OrdersServiceGrpc.SERVICE_NAME}/PostOrder"))
                    .withRequestBody(matchingJsonPath("$.direction", equalTo(orderDirection)))
            )
        requests shouldHaveSize 1
        JSONAssert.assertEquals(
            grpcRequest(fileName),
            requests[0].bodyAsString,
            CustomComparator(JSONCompareMode.STRICT, Customization("orderId") { _, _ -> true })
        )
    }

}