package ru.kcheranev.trading.domain.exception

sealed class TradingAppException(
    message: String
) : RuntimeException(message)

open class BusinessException(
    message: String
) : TradingAppException(message)

open class ValidationException(
    message: String,
    val errors: List<String>? = null
) : TradingAppException(message)

open class SystemException(
    message: String
) : TradingAppException(message)

open class InfrastructureException(
    message: String
) : TradingAppException(message)