package com.github.trading.core.port.service.command

import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.Instrument
import com.github.trading.domain.model.StrategyParameters

data class InitTradeStrategyCommand(
    val strategyType: String,
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val strategyParameters: StrategyParameters
)