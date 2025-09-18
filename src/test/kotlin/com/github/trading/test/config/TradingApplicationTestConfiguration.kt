package com.github.trading.test.config

import com.github.trading.infra.adapter.outcome.broker.impl.CandleSubscriptionCacheHolder
import com.github.trading.infra.config.BrokerApi
import com.github.trading.infra.config.properties.BrokerProperties
import com.github.trading.infra.config.properties.TelegramNotificationProperties
import com.github.trading.test.strategy.DummyTestLongStrategyFactory
import com.github.trading.test.strategy.DummyTestShortStrategyFactory
import com.github.trading.test.stub.WireMockServers.grpcWireMockServer
import com.github.trading.test.stub.http.TelegramNotificationHttpStub
import com.github.trading.test.util.MarketDataSubscriptionInitializer
import io.grpc.ManagedChannelBuilder
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import ru.tinkoff.piapi.core.stream.MarketDataStreamService

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
    fun marketDataSubscriptionInitializer(
        candleSubscriptionCacheHolder: CandleSubscriptionCacheHolder,
        marketDataStreamService: MarketDataStreamService
    ) = MarketDataSubscriptionInitializer(candleSubscriptionCacheHolder, marketDataStreamService)

    @Bean
    fun telegramNotificationHttpStub(notificationProperties: TelegramNotificationProperties) =
        TelegramNotificationHttpStub(notificationProperties)

}