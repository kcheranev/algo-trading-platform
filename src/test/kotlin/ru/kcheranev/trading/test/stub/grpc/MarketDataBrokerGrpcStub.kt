package ru.kcheranev.trading.test.stub.grpc

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import org.wiremock.grpc.dsl.WireMockGrpc.json
import org.wiremock.grpc.dsl.WireMockGrpc.method
import org.wiremock.grpc.dsl.WireMockGrpcService
import ru.kcheranev.trading.test.stub.AbstractGrpcStub
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc
import ru.tinkoff.piapi.contract.v1.MarketDataStreamServiceGrpc

class MarketDataBrokerGrpcStub(
    testName: String,
    grpcWireMockServer: WireMockServer
) : AbstractGrpcStub(testName) {

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

    fun verifyForMarketDataStream(fileName: String) {
        awaitedVerify {
            marketDataStreamService.verify("MarketDataStream")
                .withRequestMessage(equalToJson(grpcRequest(fileName)))
        }
    }

}