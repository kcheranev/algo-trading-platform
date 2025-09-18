package com.github.trading.infra.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect

@Configuration
class ThymeleafConfiguration {

    @Bean
    fun java8TimeDialect() = Java8TimeDialect()

}