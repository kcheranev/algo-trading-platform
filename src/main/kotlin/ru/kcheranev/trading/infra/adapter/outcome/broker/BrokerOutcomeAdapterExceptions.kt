package ru.kcheranev.trading.infra.adapter.outcome.broker

import ru.kcheranev.trading.domain.model.CandleInterval

abstract class BrokerOutcomeAdapterException(
    message: String
) : RuntimeException(message)

class UnexpectedCandleIntervalException(candleInterval: CandleInterval) :
    BrokerOutcomeAdapterException("Unexpected candle interval $candleInterval")