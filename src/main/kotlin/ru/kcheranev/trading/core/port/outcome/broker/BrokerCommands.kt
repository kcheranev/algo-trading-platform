package ru.kcheranev.trading.core.port.outcome.broker

import ru.kcheranev.trading.core.OutcomeCommandValidationException
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import java.time.LocalDateTime

data class PostBestPriceBuyOrderCommand(
    val instrument: Instrument,
    val quantity: Int
)

data class PostBestPriceSellOrderCommand(
    val instrument: Instrument,
    val quantity: Int
)

data class SubscribeCandlesOrderCommand(
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
        if (from.toLocalDate() != to.toLocalDate()) {
            throw OutcomeCommandValidationException("From day must be equals to to day")
        }
    }

}

data class GetHistoricCandlesForLongPeriodCommand(
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val from: LocalDateTime,
    val to: LocalDateTime
)

data class GetLastHistoricCandlesCommand(
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val quantity: Int
)