package ru.kcheranev.trading.core.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.kcheranev.trading.common.date.DateSupplier
import java.time.LocalDate
import java.time.LocalDateTime

@Configuration
class DateConfiguration {

    @Bean
    fun dateSupplier() = object : DateSupplier {

        override fun currentDateTime() = LocalDateTime.now()

        override fun currentDate() = LocalDate.now()

    }

}