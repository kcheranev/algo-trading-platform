package ru.kcheranev.trading.domain

open class DomainException(
    message: String
) : RuntimeException(message)

open class TradeSessionDomainException(
    message: String
) : DomainException(message)