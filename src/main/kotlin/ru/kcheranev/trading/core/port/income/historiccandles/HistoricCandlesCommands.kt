package ru.kcheranev.trading.core.port.income.historiccandles

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import java.time.LocalDate

data class StoreHistoricCandlesCommand(
    val instrument: Instrument,
    val from: LocalDate,
    val to: LocalDate,
    val candleInterval: CandleInterval
)