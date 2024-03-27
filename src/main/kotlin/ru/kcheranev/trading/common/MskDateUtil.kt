package ru.kcheranev.trading.common

import java.time.LocalDateTime
import java.time.ZoneId

object MskDateUtil {

    val mskZoneId: ZoneId = ZoneId.of("Europe/Moscow")

    fun toInstant(date: LocalDateTime) = date.atZone(mskZoneId).toInstant()

    fun toZonedDateTime(date: LocalDateTime) = date.atZone(mskZoneId)

}