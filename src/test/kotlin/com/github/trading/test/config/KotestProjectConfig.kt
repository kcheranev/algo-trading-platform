package com.github.trading.test.config

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.extensions.system.SystemEnvironmentProjectListener
import com.github.trading.test.stub.WireMockServers

object KotestProjectConfig : AbstractProjectConfig() {

    override fun extensions(): List<Extension> =
        listOf(
            SystemEnvironmentProjectListener(
                "application.infra.notification.telegram.apiUrl",
                "http://localhost:${WireMockServers.httpWireMockServer.port()}"
            )
        )

}