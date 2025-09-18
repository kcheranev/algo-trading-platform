package com.github.trading.core.port.income.historiccandles

import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.Instrument
import java.time.LocalDate

data class StoreHistoricCandlesCommand(
    val instrument: Instrument,
    val from: LocalDate,
    val to: LocalDate,
    val candleInterval: CandleInterval
)