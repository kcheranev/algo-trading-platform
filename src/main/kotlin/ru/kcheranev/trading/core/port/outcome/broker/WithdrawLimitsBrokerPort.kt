package ru.kcheranev.trading.core.port.outcome.broker

import arrow.core.Either
import ru.kcheranev.trading.core.error.BrokerIntegrationError
import java.math.BigDecimal

interface WithdrawLimitsBrokerPort {

    fun getWithdrawLimits(): Either<BrokerIntegrationError, BigDecimal>

}