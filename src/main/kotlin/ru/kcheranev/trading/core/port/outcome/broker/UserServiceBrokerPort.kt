package ru.kcheranev.trading.core.port.outcome.broker

import arrow.core.Either
import ru.kcheranev.trading.core.error.BrokerIntegrationError

interface UserServiceBrokerPort {

    fun getTradingAccountId(): Either<BrokerIntegrationError, String>

}