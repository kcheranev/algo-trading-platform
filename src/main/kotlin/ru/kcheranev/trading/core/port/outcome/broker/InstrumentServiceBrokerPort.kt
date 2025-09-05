package ru.kcheranev.trading.core.port.outcome.broker

import arrow.core.Either
import ru.kcheranev.trading.core.error.BrokerIntegrationError
import ru.kcheranev.trading.domain.model.Share

interface InstrumentServiceBrokerPort {

    fun getShareById(command: GetShareByIdCommand): Either<BrokerIntegrationError, Share>

}