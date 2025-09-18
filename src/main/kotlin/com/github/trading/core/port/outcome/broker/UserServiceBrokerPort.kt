package com.github.trading.core.port.outcome.broker

import arrow.core.Either
import com.github.trading.core.error.BrokerIntegrationError

interface UserServiceBrokerPort {

    fun getTradingAccountId(): Either<BrokerIntegrationError, String>

}