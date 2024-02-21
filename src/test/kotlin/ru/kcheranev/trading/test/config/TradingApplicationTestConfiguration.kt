package ru.kcheranev.trading.test.config

import io.grpc.ManagedChannelBuilder
import io.mockk.spyk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import ru.kcheranev.trading.common.DateSupplier
import ru.kcheranev.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionCounter
import ru.kcheranev.trading.infra.adapter.outcome.persistence.impl.TradeStrategyCache
import ru.kcheranev.trading.infra.config.BrokerApi
import ru.kcheranev.trading.infra.config.properties.BrokerProperties
import ru.kcheranev.trading.infra.config.properties.TelegramNotificationProperties
import ru.kcheranev.trading.test.strategy.DummyTestStrategyFactory
import ru.kcheranev.trading.test.stub.WireMockServers.grpcWireMockServer
import ru.kcheranev.trading.test.stub.http.TelegramNotificationHttpStub
import ru.kcheranev.trading.test.util.TradeSessionContextInitializer
import ru.tinkoff.piapi.core.stream.MarketDataStreamService
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
    fun dummyTestStrategyFactory() = DummyTestStrategyFactory()

    @Bean
    fun dateSupplier() = spyk(
        object : DateSupplier {
            override fun currentDate() = LocalDateTime.parse("2024-01-30T10:15:30")
        }
    )

    @Bean
    fun tradeSessionContextInitializer(
        tradeStrategyCache: TradeStrategyCache,
        candleSubscriptionCounter: CandleSubscriptionCounter,
        marketDataStreamService: MarketDataStreamService
    ) = TradeSessionContextInitializer(tradeStrategyCache, candleSubscriptionCounter, marketDataStreamService)

    @Bean
    fun telegramNotificationHttpStub(notificationProperties: TelegramNotificationProperties) =
        TelegramNotificationHttpStub(notificationProperties)

}