package com.github.trading.core.port.income.marketdata

import com.github.trading.domain.model.Candle

data class ProcessIncomeCandleCommand(
    val candle: Candle
)