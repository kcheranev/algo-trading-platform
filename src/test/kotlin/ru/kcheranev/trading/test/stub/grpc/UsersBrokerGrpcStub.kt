package ru.kcheranev.trading.test.stub.grpc

import com.github.tomakehurst.wiremock.client.WireMock
import org.wiremock.grpc.dsl.WireMockGrpc
import org.wiremock.grpc.dsl.WireMockGrpc.json
import org.wiremock.grpc.dsl.WireMockGrpcService
import ru.kcheranev.trading.test.stub.AbstractGrpcStub
import ru.kcheranev.trading.test.stub.WireMockServers.grpcWireMockServer
import ru.tinkoff.piapi.contract.v1.UsersServiceGrpc

class UsersBrokerGrpcStub(testName: String) : AbstractGrpcStub(testName) {

    private val usersService =
        WireMockGrpcService(
            WireMock(grpcWireMockServer.port()),
            UsersServiceGrpc.SERVICE_NAME
        )

    fun stubForGetAccounts(fileName: String) {
        usersService.stubFor(
            WireMockGrpc.method("GetAccounts")
                .willReturn(
                    json(grpcResponse(fileName))
                )
        )
    }

    fun stubForGetAccountsFailed() {
        usersService.stubFor(
            WireMockGrpc.method("GetAccounts")
                .willReturn(WireMockGrpc.Status.INTERNAL, "Error")
        )
    }

}