package ru.kcheranev.trading.common

import java.time.LocalDateTime

fun interface DateSupplier {

    fun currentDate(): LocalDateTime

}