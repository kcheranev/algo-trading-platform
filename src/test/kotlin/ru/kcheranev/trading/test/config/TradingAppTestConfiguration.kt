package ru.kcheranev.trading.test.config

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.grpc.ManagedChannelBuilder
import io.mockk.spyk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.util.TestSocketUtils
import org.wiremock.grpc.GrpcExtensionFactory
import ru.kcheranev.trading.common.DateSupplier
import ru.kcheranev.trading.infra.config.BrokerApi
import ru.kcheranev.trading.infra.config.BrokerProperties
import ru.kcheranev.trading.test.strategy.DummyTestStrategyFactory
import java.time.LocalDateTime

@TestConfiguration
class TradingAppTestConfiguration {

    private val grpcWireMockServerPort = TestSocketUtils.findAvailableTcpPort()

    @Bean(destroyMethod = "destroy")
    fun brokerApi(brokerProperties: BrokerProperties) =
        BrokerApi.init(
            ManagedChannelBuilder.forAddress("localhost", grpcWireMockServerPort)
                .usePlaintext()
                .build()
        )

    @Bean
    fun dummyTestStrategy() = DummyTestStrategyFactory()

    @Bean
    fun grpcWireMockServer() =
        WireMockServer(
            WireMockConfiguration.wireMockConfig()
                .port(grpcWireMockServerPort)
                .withRootDirectory("src/test/resources/wiremock")
                .extensions(GrpcExtensionFactory())
        )

    @Bean
    fun dateSupplier() = spyk(
        object : DateSupplier {
            override fun currentDate() = LocalDateTime.parse("2024-01-30T10:15:30")
        }
    )

}