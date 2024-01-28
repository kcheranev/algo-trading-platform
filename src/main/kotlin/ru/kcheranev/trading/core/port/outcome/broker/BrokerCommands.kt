package ru.kcheranev.trading.core.port.outcome.broker

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import java.time.LocalDateTime

sealed class BrokerOutcomeCommand

data class PostBestPriceBuyOrderCommand(
    val instrument: Instrument,
    val quantity: Int
) : BrokerOutcomeCommand()

data class PostBestPriceSellOrderCommand(
    val instrument: Instrument,
    val quantity: Int
) : BrokerOutcomeCommand()

data class SubscribeCandlesOrderCommand(
    val instrument: Instrument,
    val candleInterval: CandleInterval
) : BrokerOutcomeCommand()

data class UnsubscribeCandlesOrderCommand(
    val instrument: Instrument,
    val candleInterval: CandleInterval
) : BrokerOutcomeCommand()

data class GetHistoricCandlesCommand(
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val from: LocalDateTime,
    val to: LocalDateTime
) : BrokerOutcomeCommand()

data class GetLastHistoricCandlesCommand(
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val quantity: Int
) : BrokerOutcomeCommand()