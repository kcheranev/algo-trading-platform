package ru.kcheranev.trading.infra.adapter.outcome.persistence

import ru.kcheranev.trading.domain.exception.InfrastructureException

class PersistenceNotFoundException(message: String) : InfrastructureException(message)