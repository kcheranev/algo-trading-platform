package ru.kcheranev.trading.domain

abstract class DomainException(
    message: String
) : RuntimeException(message)

open class TradeSessionDomainException(
    message: String
) : DomainException(message)