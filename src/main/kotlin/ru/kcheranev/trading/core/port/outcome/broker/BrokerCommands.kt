package ru.kcheranev.trading.core.port.outcome.broker

import ru.kcheranev.trading.domain.model.CandleInterval
import java.time.LocalDateTime

sealed class BrokerCommand

data class PostBestPriceBuyOrderCommand(
    val ticker: String,
    val instrumentId: String,
    val quantity: Long
) : BrokerCommand()

data class PostBestPriceSellOrderCommand(
    val ticker: String,
    val instrumentId: String,
    val quantity: Long
) : BrokerCommand()

data class SubscribeCandlesOrderCommand(
    val ticker: String,
    val instrumentId: String,
    val candleInterval: CandleInterval
) : BrokerCommand()

data class UnsubscribeCandlesOrderCommand(
    val ticker: String,
    val instrumentId: String,
    val candleInterval: CandleInterval
) : BrokerCommand()

data class CheckCandlesSubscriptionExistsCommand(
    val ticker: String,
    val candleInterval: CandleInterval
) : BrokerCommand()

data class GetHistoricCandlesCommand(
    val ticker: String,
    val instrumentId: String,
    val candleInterval: CandleInterval,
    val from: LocalDateTime,
    val to: LocalDateTime = LocalDateTime.now()
) : BrokerCommand()

data class GetLastHistoricCandlesCommand(
    val ticker: String,
    val instrumentId: String,
    val candleInterval: CandleInterval,
    val quantity: Int
) : BrokerCommand()