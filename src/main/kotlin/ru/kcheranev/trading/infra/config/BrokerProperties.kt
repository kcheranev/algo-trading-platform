package ru.kcheranev.trading.infra.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("application.infra.broker")
class BrokerProperties @ConstructorBinding constructor(
    val token: String,
    val tradingAccountName: String
)