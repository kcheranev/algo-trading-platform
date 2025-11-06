package com.github.trading.common.date

import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

val mskZoneId: ZoneId = ZoneId.of("Europe/Moscow")

fun LocalDateTime.utcAsMskLocalDateTime(): LocalDateTime = plusHours(3)

fun LocalDateTime.toMskInstant(): Instant = atZone(mskZoneId).toInstant()

fun LocalDateTime.toMskZonedDateTime(): ZonedDateTime = atZone(mskZoneId)

fun Instant.toMstLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this, mskZoneId)

fun Instant.toMskLocalTime(): LocalTime = LocalTime.ofInstant(this, mskZoneId)