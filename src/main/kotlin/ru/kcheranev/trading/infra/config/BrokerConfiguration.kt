package ru.kcheranev.trading.infra.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.kcheranev.trading.infra.adapter.outcome.broker.logging.LoggingOrdersServiceDecorator
import ru.kcheranev.trading.infra.config.properties.BrokerProperties
import ru.tinkoff.piapi.core.OrdersService

@Configuration
class BrokerConfiguration {

    @Bean(destroyMethod = "destroy")
    fun brokerApi(brokerProperties: BrokerProperties) =
        BrokerApi.init(brokerProperties.token, brokerProperties.appName)

    @Bean
    fun ordersService(brokerApi: BrokerApi) = brokerApi.investApi.ordersService

    @Bean
    fun loggingOrderServiceDecorator(ordersService: OrdersService) = LoggingOrdersServiceDecorator(ordersService)

    @Bean
    fun usersService(brokerApi: BrokerApi) = brokerApi.investApi.userService

    @Bean
    fun marketDataStreamService(brokerApi: BrokerApi) = brokerApi.investApi.marketDataStreamService

    @Bean
    fun marketDataService(brokerApi: BrokerApi) = brokerApi.investApi.marketDataService

}