package ru.kcheranev.trading.core.port.income.marketdata

import ru.kcheranev.trading.domain.model.Candle

data class ProcessIncomeCandleCommand(
    val candle: Candle
)