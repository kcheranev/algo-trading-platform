package ru.kcheranev.trading.core.port.outcome.broker

import arrow.core.Either
import ru.kcheranev.trading.core.error.BrokerIntegrationError
import ru.kcheranev.trading.domain.model.Portfolio

interface OperationServiceBrokerPort {

    fun getPortfolio(): Either<BrokerIntegrationError, Portfolio>

}