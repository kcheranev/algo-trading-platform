package ru.kcheranev.trading.core

open class BusinessException(
    message: String
) : RuntimeException(message)

class StrategyParamValidationException(message: String) : BusinessException(message)

class IncomeCommandValidationException(message: String) : BusinessException(message)

class OutcomeCommandValidationException(message: String) : BusinessException(message)