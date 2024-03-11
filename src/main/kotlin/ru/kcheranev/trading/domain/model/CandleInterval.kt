package ru.kcheranev.trading.domain.model

import java.time.Duration
import java.time.temporal.ChronoUnit

enum class CandleInterval(
    val duration: Duration,
    val chronoUnit: ChronoUnit
) {

    UNDEFINED(Duration.ZERO, ChronoUnit.SECONDS),
    ONE_MIN(Duration.ofMinutes(1), ChronoUnit.MINUTES),
    FIVE_MIN(Duration.ofMinutes(5), ChronoUnit.MINUTES)

}