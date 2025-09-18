package com.github.trading.test.stub.grpc

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.trading.test.stub.AbstractGrpcStub
import com.github.trading.test.stub.WireMockServers.grpcWireMockServer
import org.wiremock.grpc.dsl.WireMockGrpc
import org.wiremock.grpc.dsl.WireMockGrpc.json
import org.wiremock.grpc.dsl.WireMockGrpcService
import ru.tinkoff.piapi.contract.v1.OperationsServiceGrpc

class OperationsBrokerGrpcStub(testName: String) : AbstractGrpcStub(testName) {

    private val operationsService =
        WireMockGrpcService(
            WireMock(grpcWireMockServer.port()),
            OperationsServiceGrpc.SERVICE_NAME
        )

    fun stubForGetPortfolio(fileName: String) {
        operationsService.stubFor(
            WireMockGrpc.method("GetPortfolio")
                .willReturn(
                    json(grpcResponse(fileName))
                )
        )
    }

}