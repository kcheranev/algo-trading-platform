package ru.kcheranev.trading.core.port.income

import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.StrategyType

sealed class TradingCommand

data class StartTradeSessionCommand(
    val strategyConfigurationId: StrategyConfigurationId,
    val ticker: String,
    val instrumentId: String,
    val strategyType: StrategyType
) : TradingCommand()

data class ProcessIncomeCandleCommand(
    val candle: Candle
) : TradingCommand()