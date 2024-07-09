package ru.kcheranev.trading.common.date

import java.time.LocalDateTime

fun interface DateSupplier {

    fun currentDate(): LocalDateTime

}