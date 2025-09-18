package com.github.trading.core.port.outcome.broker

import arrow.core.Either
import com.github.trading.core.error.BrokerIntegrationError
import com.github.trading.domain.model.Share

interface InstrumentServiceBrokerPort {

    fun getShareById(command: GetShareByIdCommand): Either<BrokerIntegrationError, Share>

}