package ru.kcheranev.trading.core.port.outcome.broker

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import java.time.LocalDateTime

sealed class BrokerCommand

data class PostBestPriceBuyOrderCommand(
    val instrument: Instrument,
    val quantity: Int
) : BrokerCommand()

data class PostBestPriceSellOrderCommand(
    val instrument: Instrument,
    val quantity: Int
) : BrokerCommand()

data class SubscribeCandlesOrderCommand(
    val instrument: Instrument,
    val candleInterval: CandleInterval
) : BrokerCommand()

data class UnsubscribeCandlesOrderCommand(
    val instrument: Instrument,
    val candleInterval: CandleInterval
) : BrokerCommand()

data class GetHistoricCandlesCommand(
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val from: LocalDateTime,
    val to: LocalDateTime = LocalDateTime.now()
) : BrokerCommand()

data class GetLastHistoricCandlesCommand(
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val quantity: Int
) : BrokerCommand()