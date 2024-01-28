package ru.kcheranev.trading.test.config

import com.github.tomakehurst.wiremock.WireMockServer
import io.kotest.extensions.wiremock.ListenerMode
import io.kotest.extensions.wiremock.WireMockListener
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class ListenerTestConfiguration {

    @Bean
    fun grpcWireMockListener(grpcWireMockServer: WireMockServer) =
        WireMockListener(grpcWireMockServer, ListenerMode.PER_SPEC)

}