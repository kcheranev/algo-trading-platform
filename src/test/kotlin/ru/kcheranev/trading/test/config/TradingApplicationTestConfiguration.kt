package ru.kcheranev.trading.test.config

import io.grpc.ManagedChannelBuilder
import io.mockk.spyk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import ru.kcheranev.trading.common.date.DateSupplier
import ru.kcheranev.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionHolder
import ru.kcheranev.trading.infra.config.BrokerApi
import ru.kcheranev.trading.infra.config.properties.BrokerProperties
import ru.kcheranev.trading.infra.config.properties.TelegramNotificationProperties
import ru.kcheranev.trading.test.strategy.DummyTestLongStrategyFactory
import ru.kcheranev.trading.test.strategy.DummyTestShortStrategyFactory
import ru.kcheranev.trading.test.stub.WireMockServers.grpcWireMockServer
import ru.kcheranev.trading.test.stub.http.TelegramNotificationHttpStub
import ru.kcheranev.trading.test.util.MarketDataSubscriptionInitializer
import ru.tinkoff.piapi.core.stream.MarketDataStreamService
import java.time.LocalDate
import java.time.LocalDateTime

@TestConfiguration
class TradingApplicationTestConfiguration {

    @Bean(destroyMethod = "destroy")
    fun brokerApi(brokerProperties: BrokerProperties) =
        BrokerApi.init(
            ManagedChannelBuilder.forAddress("localhost", grpcWireMockServer.port())
                .usePlaintext()
                .build()
        )

    @Bean
    fun dummyTestLongStrategyFactory() = DummyTestLongStrategyFactory()

    @Bean
    fun dummyTestShortStrategyFactory() = DummyTestShortStrategyFactory()

    @Bean
    fun dateSupplier() = spyk(
        object : DateSupplier {

            override fun currentDateTime() = LocalDateTime.parse("2024-01-30T10:15:30")

            override fun currentDate() = LocalDate.parse("2024-01-30")

        }
    )

    @Bean
    fun marketDataSubscriptionInitializer(
        candleSubscriptionHolder: CandleSubscriptionHolder,
        marketDataStreamService: MarketDataStreamService
    ) = MarketDataSubscriptionInitializer(candleSubscriptionHolder, marketDataStreamService)

    @Bean
    fun telegramNotificationHttpStub(notificationProperties: TelegramNotificationProperties) =
        TelegramNotificationHttpStub(notificationProperties)

}