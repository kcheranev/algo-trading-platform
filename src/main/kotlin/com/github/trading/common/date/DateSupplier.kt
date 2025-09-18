package com.github.trading.common.date

import java.time.LocalDate
import java.time.LocalDateTime

object DateSupplier {

    fun currentDateTime(): LocalDateTime = LocalDateTime.now()

    fun currentDate(): LocalDate = LocalDate.now()

}