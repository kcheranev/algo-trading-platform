package ru.kcheranev.trading.core.port.income.trading

import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.Instrument
import ru.kcheranev.trading.domain.model.StrategyType

sealed class TradingCommand

data class StartTradeSessionCommand(
    val strategyConfigurationId: StrategyConfigurationId,
    val lotsQuantity: Int,
    val instrument: Instrument,
    val strategyType: StrategyType
) : TradingCommand()

data class ProcessIncomeCandleCommand(
    val candle: Candle
) : TradingCommand()

data class EnterTradeSessionCommand(
    val tradeSessionId: TradeSessionId
) : TradingCommand()

data class ExitTradeSessionCommand(
    val tradeSessionId: TradeSessionId
) : TradingCommand()