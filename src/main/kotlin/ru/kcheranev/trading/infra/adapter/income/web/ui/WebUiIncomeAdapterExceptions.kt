package ru.kcheranev.trading.infra.adapter.income.web.ui

import ru.kcheranev.trading.domain.exception.InfrastructureException

class NotFoundException(message: String) : InfrastructureException(message)