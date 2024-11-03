package ru.kcheranev.trading.core.exception

import ru.kcheranev.trading.domain.exception.BusinessException

class OutcomeCommandValidationException(message: String) : BusinessException(message)