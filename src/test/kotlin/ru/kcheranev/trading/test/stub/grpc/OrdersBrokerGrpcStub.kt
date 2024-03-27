package ru.kcheranev.trading.test.stub.grpc

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
import ru.kcheranev.trading.test.stub.WireMockServers.grpcWireMockServer
import ru.tinkoff.piapi.contract.v1.OrderDirection
import ru.tinkoff.piapi.contract.v1.OrdersServiceGrpc

class OrdersBrokerGrpcStub(testName: String) : AbstractGrpcStub(testName) {

    private val ordersService =
        WireMockGrpcService(
            WireMock(grpcWireMockServer.port()),
            OrdersServiceGrpc.SERVICE_NAME
        )

    fun stubForPostBuyOrder(fileName: String, matchConditions: Map<String, String> = emptyMap()) {
        val newMatchConditions = matchConditions.toMutableMap()
        newMatchConditions["$.direction"] = OrderDirection.ORDER_DIRECTION_BUY.name
        stubForPostOrder(fileName, newMatchConditions)
    }

    fun stubForPostSellOrder(fileName: String, matchConditions: Map<String, String> = emptyMap()) {
        val newMatchConditions = matchConditions.toMutableMap()
        newMatchConditions["$.direction"] = OrderDirection.ORDER_DIRECTION_SELL.name
        stubForPostOrder(fileName, newMatchConditions)
    }

    private fun stubForPostOrder(responseFileName: String, matchConditions: Map<String, String> = emptyMap()) {
        ordersService.stubFor(
            WireMockGrpc.method("PostOrder")
                .also {
                    matchConditions.forEach { (jsonPath, expectedValue) ->
                        it.withRequestMessage(matchingJsonPath(jsonPath, equalTo(expectedValue)))
                    }
                }
                .willReturn(json(grpcResponse(responseFileName)))
        )
    }

    fun verifyForPostBuyOrder(
        fileName: String,
        matchConditions: Map<String, String> = emptyMap(),
        queryCount: Int = 1
    ) {
        val newMatchConditions = matchConditions.toMutableMap()
        newMatchConditions["$.direction"] = OrderDirection.ORDER_DIRECTION_BUY.name
        verifyForPostOrder(fileName, newMatchConditions, queryCount)
    }

    fun verifyForPostSellOrder(
        fileName: String,
        matchConditions: Map<String, String> = emptyMap(),
        queryCount: Int = 1
    ) {
        val newMatchConditions = matchConditions.toMutableMap()
        newMatchConditions["$.direction"] = OrderDirection.ORDER_DIRECTION_SELL.name
        verifyForPostOrder(fileName, newMatchConditions, queryCount)
    }

    private fun verifyForPostOrder(
        fileName: String,
        matchConditions: Map<String, String> = emptyMap(),
        queryCount: Int = 1
    ) {
        val requests =
            grpcWireMockServer.findAll(
                postRequestedFor(urlMatching("/${OrdersServiceGrpc.SERVICE_NAME}/PostOrder"))
                    .also {
                        matchConditions.forEach { (jsonPath, expectedValue) ->
                            it.withRequestBody(matchingJsonPath(jsonPath, equalTo(expectedValue)))
                        }
                    }
            )
        requests shouldHaveSize queryCount
        JSONAssert.assertEquals(
            grpcRequest(fileName),
            requests[0].bodyAsString,
            CustomComparator(JSONCompareMode.STRICT, Customization("orderId") { _, _ -> true })
        )
    }

}