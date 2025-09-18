package com.github.trading.core.port.outcome.persistence.instrument

import com.github.trading.domain.entity.Instrument
import com.github.trading.domain.entity.InstrumentId

data class InsertInstrumentCommand(
    val instrument: Instrument
)

data class GetInstrumentCommand(
    val instrumentId: InstrumentId
)

data class GetInstrumentByBrokerInstrumentIdCommand(
    val brokerInstrumentId: String
)