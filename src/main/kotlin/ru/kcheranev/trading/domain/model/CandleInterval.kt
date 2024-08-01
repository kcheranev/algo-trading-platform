package ru.kcheranev.trading.domain.model

import java.time.Duration

enum class CandleInterval(val duration: Duration) {

    ONE_MIN(Duration.ofMinutes(1)),
    FIVE_MIN(Duration.ofMinutes(5))

}