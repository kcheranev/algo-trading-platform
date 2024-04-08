package ru.kcheranev.trading.core

open class BusinessException(
    message: String
) : RuntimeException(message)

class StrategyValidationException(message: String) : BusinessException(message)