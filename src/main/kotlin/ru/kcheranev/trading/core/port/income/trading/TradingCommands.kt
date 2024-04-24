package ru.kcheranev.trading.core.port.income.trading

import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.StrategyParameters

data class CreateStrategyConfigurationCommand(
    val type: String,
    val candleInterval: CandleInterval,
    val params: StrategyParameters
)

data class StartTradeSessionCommand(
    val strategyConfigurationId: StrategyConfigurationId,
    val lotsQuantity: Int,
    val instrument: Instrument
)

data class ProcessIncomeCandleCommand(
    val candle: Candle
)

data class EnterTradeSessionCommand(
    val tradeSessionId: TradeSessionId
)

data class ExitTradeSessionCommand(
    val tradeSessionId: TradeSessionId
)

data class StopTradeSessionCommand(
    val tradeSessionId: TradeSessionId
)