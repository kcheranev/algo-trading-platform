package ru.kcheranev.trading.common.date

import java.time.LocalDate
import java.time.LocalDateTime

interface DateSupplier {

    fun currentDateTime(): LocalDateTime

    fun currentDate(): LocalDate

}