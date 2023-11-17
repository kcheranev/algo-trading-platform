package ru.kcheranev.trading.infra.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BrokerConfig {

    @Bean(destroyMethod = "destroy")
    fun brokerApi(brokerProperties: BrokerProperties): BrokerApi {
        return BrokerApi(brokerProperties.token)
    }

}