package ru.kcheranev.trading.core.port.income.trading

import ru.kcheranev.trading.core.port.outcome.broker.model.PostOrderStatus
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.model.Candle
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import java.math.BigDecimal

sealed class TradingIncomeCommand

data class CreateStrategyConfigurationCommand(
    val type: String,
    val initCandleAmount: Int,
    val candleInterval: CandleInterval,
    val params: Map<String, Any>
) : TradingIncomeCommand()

data class StartTradeSessionCommand(
    val strategyConfigurationId: StrategyConfigurationId,
    val lotsQuantity: Int,
    val instrument: Instrument
) : TradingIncomeCommand()

data class ProcessIncomeCandleCommand(
    val candle: Candle
) : TradingIncomeCommand()

data class EnterTradeSessionCommand(
    val tradeSessionId: TradeSessionId,
    val status: PostOrderStatus,
    val lotsRequested: Long,
    val lotsExecuted: Long,
    val totalPrice: BigDecimal,
    val executedCommission: BigDecimal
) : TradingIncomeCommand()

data class ExitTradeSessionCommand(
    val tradeSessionId: TradeSessionId,
    val status: PostOrderStatus,
    val lotsRequested: Long,
    val lotsExecuted: Long,
    val totalPrice: BigDecimal,
    val executedCommission: BigDecimal
) : TradingIncomeCommand()

data class StopTradeSessionCommand(
    val tradeSessionId: TradeSessionId
) : TradingIncomeCommand()