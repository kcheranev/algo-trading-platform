package com.github.trading.test.config

import com.github.trading.infra.config.properties.TelegramNotificationProperties
import com.github.trading.test.strategy.DummyTestLongStrategyFactory
import com.github.trading.test.strategy.DummyTestShortStrategyFactory
import com.github.trading.test.stub.http.TelegramNotificationHttpStub
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import ru.ttech.piapi.core.connector.ConnectorConfiguration
import ru.ttech.piapi.core.connector.ServiceStubFactory
import java.lang.reflect.Field
import java.util.function.Supplier

@TestConfiguration
class TradingApplicationTestConfiguration {

    @Bean
    fun dummyTestLongStrategyFactory(): DummyTestLongStrategyFactory = DummyTestLongStrategyFactory()

    @Bean
    fun dummyTestShortStrategyFactory(): DummyTestShortStrategyFactory = DummyTestShortStrategyFactory()

    @Bean
    fun testBeanPostProcessor(): TestBeanPostProcessor = TestBeanPostProcessor()

    @Bean
    fun serviceStubFactory(connectorConfiguration: ConnectorConfiguration): ServiceStubFactory =
        ServiceStubFactory.create(connectorConfiguration)
            .also { serviceStubFactory ->
                val insecureChannel =
                    NettyChannelBuilder
                        .forTarget(connectorConfiguration.targetUrl)
                        .usePlaintext()
                        .build()
                val supplierField: Field = ServiceStubFactory::class.java.getDeclaredField("supplier")
                supplierField.setAccessible(true)
                supplierField.set(serviceStubFactory, Supplier { insecureChannel })
            }

    @Bean
    fun telegramNotificationHttpStub(notificationProperties: TelegramNotificationProperties): TelegramNotificationHttpStub =
        TelegramNotificationHttpStub(notificationProperties)

}