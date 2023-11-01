package ru.kcheranev.trading

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class TradingApplication

fun main(args: Array<String>) {
    runApplication<TradingApplication>(*args)
}