package ru.kcheranev.trading.core.exception

import ru.kcheranev.trading.domain.exception.ValidationException

class StrategyParamValidationException(message: String) : ValidationException(message)