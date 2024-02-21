package ru.kcheranev.trading.test.stub

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.wiremock.grpc.GrpcExtensionFactory

object WireMockServers {

    val httpWireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())

    val grpcWireMockServer =
        WireMockServer(
            WireMockConfiguration.options()
                .dynamicPort()
                .withRootDirectory("src/test/resources/wiremock")
                .extensions(GrpcExtensionFactory())
        )

    init {
        httpWireMockServer.start()
        grpcWireMockServer.start()
    }

}