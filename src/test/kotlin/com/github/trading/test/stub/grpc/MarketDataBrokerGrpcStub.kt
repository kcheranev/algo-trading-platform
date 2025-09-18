package com.github.trading.test.stub.grpc

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.trading.test.stub.AbstractGrpcStub
import com.github.trading.test.stub.WireMockServers.grpcWireMockServer
import io.kotest.assertions.nondeterministic.eventually
import org.wiremock.grpc.dsl.WireMockGrpc.json
import org.wiremock.grpc.dsl.WireMockGrpc.method
import org.wiremock.grpc.dsl.WireMockGrpcService
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc
import ru.tinkoff.piapi.contract.v1.MarketDataStreamServiceGrpc
import kotlin.time.Duration.Companion.seconds

class MarketDataBrokerGrpcStub(testName: String) : AbstractGrpcStub(testName) {

    private val marketDataService =
        WireMockGrpcService(
            WireMock(grpcWireMockServer.port()),
            MarketDataServiceGrpc.SERVICE_NAME
        )

    private val marketDataStreamService =
        WireMockGrpcService(
            WireMock(grpcWireMockServer.port()),
            MarketDataStreamServiceGrpc.SERVICE_NAME
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

    suspend fun verifyForMarketDataStream(fileName: String) {
        eventually(3.seconds) {
            marketDataStreamService.verify("MarketDataStream")
                .withRequestMessage(equalToJson(grpcRequest(fileName)))
        }
    }

}