package ru.kcheranev.trading.infra.adapter.outcome.persistence

open class PersistenceOutcomeAdapterException(
    message: String
) : RuntimeException(message)

class PersistenceNotFoundException(message: String) : PersistenceOutcomeAdapterException(message)