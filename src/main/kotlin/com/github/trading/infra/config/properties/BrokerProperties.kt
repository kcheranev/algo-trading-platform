package com.github.trading.infra.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("application.infra.broker")
class BrokerProperties @ConstructorBinding constructor(
    val tradingAccountName: String
)