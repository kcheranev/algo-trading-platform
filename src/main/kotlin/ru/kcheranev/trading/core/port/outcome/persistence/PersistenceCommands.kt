package ru.kcheranev.trading.core.port.outcome.persistence

import ru.kcheranev.trading.core.port.common.model.ComparedField
import ru.kcheranev.trading.core.port.common.model.Page
import ru.kcheranev.trading.core.port.common.model.Sort
import ru.kcheranev.trading.domain.entity.Order
import ru.kcheranev.trading.domain.entity.OrderId
import ru.kcheranev.trading.domain.entity.OrderSort
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.entity.StrategyConfigurationSort
import ru.kcheranev.trading.domain.entity.TradeDirection
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.entity.TradeSessionSort
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyType
import java.math.BigDecimal
import java.time.LocalDateTime

sealed class PersistenceOutcomeCommand

data class SaveOrderCommand(
    val order: Order
) : PersistenceOutcomeCommand()

data class GetOrderCommand(
    val orderId: OrderId
) : PersistenceOutcomeCommand()

data class OrderSearchCommand(
    val id: OrderId?,
    val ticker: String?,
    val instrumentId: String?,
    val date: ComparedField<LocalDateTime>?,
    val quantity: ComparedField<Int>?,
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
    val type: StrategyType?,
    val candleInterval: CandleInterval?,
    val page: Page?,
    val sort: Sort<StrategyConfigurationSort>?
) : PersistenceOutcomeCommand()