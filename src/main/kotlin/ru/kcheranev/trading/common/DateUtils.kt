package ru.kcheranev.trading.common

import java.time.DayOfWeek
import java.time.LocalDate

fun LocalDate.isWeekend() = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY