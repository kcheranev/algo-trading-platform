package com.github.trading.common.date

import com.github.trading.core.config.TradingProperties.Companion.tradingProperties
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun LocalDate.isWeekend() = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY

fun LocalDate.atEndOfDay(): LocalDateTime = LocalDateTime.of(this, LocalTime.MAX)

fun min(time1: LocalTime, time2: LocalTime) = if (time1 < time2) time1 else time2

fun max(time1: LocalTime, time2: LocalTime) = if (time1 > time2) time1 else time2

fun isTradingTime(): Boolean {
    val now = DateSupplier.currentDateTime()
    if (now.toLocalDate().isWeekend()) {
        return false
    }
    val currentTime = now.toLocalTime()
    return tradingProperties.tradingSchedule
        .any { timeInterval -> currentTime > timeInterval.from && currentTime < timeInterval.to }
}