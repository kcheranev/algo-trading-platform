package ru.kcheranev.trading.common

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

val mskZoneId: ZoneId = ZoneId.of("Europe/Moscow")

fun LocalDateTime.toMskInstant(): Instant = atZone(mskZoneId).toInstant()

fun LocalDateTime.toMskZonedDateTime(): ZonedDateTime = atZone(mskZoneId)