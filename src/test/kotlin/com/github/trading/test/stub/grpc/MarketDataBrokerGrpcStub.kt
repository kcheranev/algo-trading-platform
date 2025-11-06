package com.github.trading.test.stub.grpc

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.trading.test.stub.AbstractGrpcStub
import com.github.trading.test.stub.WireMockServers.grpcWireMockServer
import org.wiremock.grpc.dsl.WireMockGrpc.json
import org.wiremock.grpc.dsl.WireMockGrpc.method
import org.wiremock.grpc.dsl.WireMockGrpcService
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc

class MarketDataBrokerGrpcStub(testName: String) : AbstractGrpcStub(testName) {

    private val marketDataService =
        WireMockGrpcService(
            WireMock(grpcWireMockServer.port()),
            MarketDataServiceGrpc.SERVICE_NAME
        )

    fun stubForGetCandles(fileName: String) {
        marketDataService.stubFor(
            method("GetCandles")
                .willReturn(
                    json(grpcResponse(fileName))
                )
        )
    }

    fun verifyForGetCandles(fileName: String) {
        marketDataService.verify("GetCandles")
            .withRequestMessage(equalToJson(grpcRequest(fileName)))
    }

}