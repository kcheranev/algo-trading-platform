package ru.kcheranev.trading.infra.adapter.outcome

import ru.kcheranev.trading.domain.model.CandleInterval

abstract class OutcomeAdapterException(
    message: String
) : RuntimeException(message)

open class BrokerOutcomeAdapterException(
    message: String
) : OutcomeAdapterException(message)

class UnexpectedCandleIntervalException(candleInterval: CandleInterval) :
    BrokerOutcomeAdapterException("Unexpected candle interval $candleInterval")

open class PersistenceOutcomeAdapterException(
    message: String
) : OutcomeAdapterException(message)