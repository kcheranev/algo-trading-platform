package com.github.trading.infra.adapter.outcome.persistence

import com.github.trading.domain.exception.InfrastructureException

class PersistenceNotFoundException(message: String) : InfrastructureException(message)