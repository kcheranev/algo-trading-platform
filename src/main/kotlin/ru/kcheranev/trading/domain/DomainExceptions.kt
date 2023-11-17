package ru.kcheranev.trading.domain

import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.entity.TradeSessionStatus

abstract class DomainException(
    message: String
) : RuntimeException(message)

class TradeSessionNotExistsException :
    DomainException("Trade session is not exists")

class UnexpectedTradeSessionTransitionException(
    id: TradeSessionId,
    from: TradeSessionStatus,
    to: TradeSessionStatus
) : DomainException("Unexpected trade session ${id.value} status transition from $from to $to")