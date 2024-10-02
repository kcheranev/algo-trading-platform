package ru.kcheranev.trading.core.port.service.command

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.StrategyParameters

data class InitTradeStrategyCommand(
    val strategyType: String,
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val strategyParameters: StrategyParameters
)