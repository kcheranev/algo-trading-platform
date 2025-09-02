package ru.kcheranev.trading.infra.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties("application.backtesting")
data class BacktestingProperties @ConstructorBinding constructor(
    val tempFileDirectory: String
)