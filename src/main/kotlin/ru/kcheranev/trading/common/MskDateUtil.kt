package ru.kcheranev.trading.common

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

object MskDateUtil {

    val mskZoneId: ZoneId = ZoneId.of("Europe/Moscow")

    fun toInstant(date: LocalDateTime): Instant = date.atZone(mskZoneId).toInstant()

    fun toZonedDateTime(date: LocalDateTime): ZonedDateTime = date.atZone(mskZoneId)

}