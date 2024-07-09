package ru.kcheranev.trading.common.date

import java.time.DayOfWeek
import java.time.LocalDate

fun LocalDate.isWeekend() = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY