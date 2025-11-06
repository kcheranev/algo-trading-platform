package com.github.trading.test.config

import com.github.trading.test.stub.WireMockServers.grpcWireMockServer
import com.github.trading.test.stub.WireMockServers.httpWireMockServer
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.extensions.spring.SpringExtension
import io.kotest.extensions.system.SystemEnvironmentProjectListener

object ProjectConfig : AbstractProjectConfig() {

    override val extensions: List<Extension> =
        listOf(
            SpringExtension(),
            SystemEnvironmentProjectListener(
                mapOf(
                    "application.infra.notification.telegram.apiUrl" to "http://localhost:${httpWireMockServer.port()}",
                    "invest.connector.targetUrl" to "localhost:${grpcWireMockServer.port()}"
                )
            )
        )

}