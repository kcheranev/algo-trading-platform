package com.github.trading.core.port.outcome.broker

import com.github.trading.core.util.Validator.Companion.validateOrThrow
import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.Instrument
import java.time.LocalDate
import java.time.LocalDateTime

data class PostBestPriceBuyOrderCommand(
    val instrument: Instrument,
    val quantity: Int
)

data class PostBestPriceSellOrderCommand(
    val instrument: Instrument,
    val quantity: Int
)

data class GetMaxLotsCommand(
    val instrument: Instrument
)

data class SubscribeCandlesCommand(
    val instrument: Instrument,
    val candleInterval: CandleInterval
)

data class UnsubscribeCandlesOrderCommand(
    val instrument: Instrument,
    val candleInterval: CandleInterval
)

data class GetHistoricCandlesCommand(
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val from: LocalDateTime,
    val to: LocalDateTime
) {

    init {
        validateOrThrow {
            if (from.toLocalDate() != to.toLocalDate()) addError("From day must be equals to to day")
        }
    }

}

data class GetHistoricCandlesForLongPeriodCommand(
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val from: LocalDate,
    val to: LocalDate
)

data class GetLastHistoricCandlesCommand(
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val quantity: Int
)

data class GetShareByIdCommand(
    val instrumentId: String
)