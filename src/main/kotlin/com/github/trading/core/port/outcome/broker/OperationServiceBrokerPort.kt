package com.github.trading.core.port.outcome.broker

import arrow.core.Either
import com.github.trading.core.error.IntegrationError.BrokerIntegrationError
import com.github.trading.domain.model.Portfolio

interface OperationServiceBrokerPort {

    fun getPortfolio(): Either<BrokerIntegrationError, Portfolio>

}