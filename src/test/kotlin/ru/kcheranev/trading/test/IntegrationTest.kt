package ru.kcheranev.trading.test

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*
import org.springframework.test.context.ActiveProfiles
import ru.kcheranev.trading.test.config.ExtensionTestConfiguration
import ru.kcheranev.trading.test.config.TradingAppTestConfiguration

@Target(AnnotationTarget.CLASS)

@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    classes = [TradingAppTestConfiguration::class, ExtensionTestConfiguration::class]
)
@ActiveProfiles("test")
annotation class IntegrationTest()