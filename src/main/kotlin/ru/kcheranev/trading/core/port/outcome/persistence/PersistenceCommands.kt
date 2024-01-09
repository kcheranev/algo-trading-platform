package ru.kcheranev.trading.core.port.outcome.persistence

import ru.kcheranev.trading.core.port.common.model.ComparedField
import ru.kcheranev.trading.core.port.common.model.Page
import ru.kcheranev.trading.core.port.common.model.Sort
import ru.kcheranev.trading.domain.entity.*
import ru.kcheranev.trading.domain.model.CandleInterval
import java.math.BigDecimal
import java.time.LocalDateTime

sealed class PersistenceOutcomeCommand

data class SaveOrderCommand(
    val tradeOrder: TradeOrder
) : PersistenceOutcomeCommand()

data class GetOrderCommand(
    val tradeOrderId: TradeOrderId
) : PersistenceOutcomeCommand()

data class TradeOrderSearchCommand(
    val id: TradeOrderId?,
    val ticker: String?,
    val instrumentId: String?,
    val date: ComparedField<LocalDateTime>?,
    val lotsQuantity: ComparedField<Int>?,
    val price: ComparedField<BigDecimal>?,
    val direction: TradeDirection?,
    val tradeSessionId: TradeSessionId?,
    val page: Page?,
    val sort: Sort<OrderSort>?
) : PersistenceOutcomeCommand()

data class SaveTradeSessionCommand(
    val tradeSession: TradeSession
) : PersistenceOutcomeCommand()

data class GetTradeSessionCommand(
    val tradeSessionId: TradeSessionId
) : PersistenceOutcomeCommand()

data class GetReadyToOrderTradeSessionsCommand(
    val instrumentId: String,
    val candleInterval: CandleInterval
) : PersistenceOutcomeCommand()

data class TradeSessionSearchCommand(
    val id: TradeSessionId?,
    val ticker: String?,
    val instrumentId: String?,
    val status: TradeSessionStatus?,
    val startDate: ComparedField<LocalDateTime>?,
    val candleInterval: CandleInterval?,
    val page: Page?,
    val sort: Sort<TradeSessionSort>?
) : PersistenceOutcomeCommand()

data class SaveStrategyConfigurationCommand(
    val strategyConfiguration: StrategyConfiguration
) : PersistenceOutcomeCommand()

data class GetStrategyConfigurationCommand(
    val strategyConfigurationId: StrategyConfigurationId
) : PersistenceOutcomeCommand()

data class StrategyConfigurationSearchCommand(
    val id: StrategyConfigurationId?,
    val type: String?,
    val candleInterval: CandleInterval?,
    val page: Page?,
    val sort: Sort<StrategyConfigurationSort>?
) : PersistenceOutcomeCommand()