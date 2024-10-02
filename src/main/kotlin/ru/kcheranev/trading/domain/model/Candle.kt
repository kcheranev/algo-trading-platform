package ru.kcheranev.trading.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Candle(
    val interval: CandleInterval,
    val openPrice: BigDecimal,
    val closePrice: BigDecimal,
    val highestPrice: BigDecimal,
    val lowestPrice: BigDecimal,
    val volume: Long,
    val endDateTime: LocalDateTime,
    val instrumentId: String
) {

    override fun toString() =
        "[interval=${interval}, openPrice=$openPrice, closePrice=$closePrice, endDateTime=$endDateTime, instrumentId=$instrumentId]"

}