package ru.kcheranev.trading.core.port.outcome.broker

sealed class BrokerOutcomeCommand

data class PostBestPriceBuyOrderBrokerOutcomeCommand(
    val instrumentId: String,
    val quantity: Long
) : BrokerOutcomeCommand()

data class PostBestPriceSellOrderBrokerOutcomeCommand(
    val instrumentId: String,
    val quantity: Long
) : BrokerOutcomeCommand()