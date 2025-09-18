package com.github.trading.test

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import com.github.trading.test.config.ExtensionTestConfiguration
import com.github.trading.test.config.TradingApplicationTestConfiguration

@Target(AnnotationTarget.CLASS)

@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    classes = [
        TradingApplicationTestConfiguration::class,
        ExtensionTestConfiguration::class
    ]
)
@ActiveProfiles("test")
annotation class IntegrationTest