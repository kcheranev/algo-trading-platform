package ru.kcheranev.trading.core.port.outcome.persistence

import ru.kcheranev.trading.domain.entity.Order
import ru.kcheranev.trading.domain.entity.OrderId
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.model.CandleInterval

sealed class PersistenceCommand

data class SaveOrderCommand(
    val order: Order
) : PersistenceCommand()

data class GetOrderCommand(
    val orderId: OrderId
) : PersistenceCommand()

data class SaveTradeSessionCommand(
    val tradeSession: TradeSession
) : PersistenceCommand()

data class GetTradeSessionCommand(
    val tradeSessionId: TradeSessionId
) : PersistenceCommand()

data class GetReadyToOrderTradeSessionsCommand(
    val instrumentId: String,
    val candleInterval: CandleInterval
) : PersistenceCommand()

data class SaveStrategyConfigurationCommand(
    val strategyConfiguration: StrategyConfiguration
) : PersistenceCommand()

data class GetStrategyConfigurationCommand(
    val strategyConfigurationId: StrategyConfigurationId
) : PersistenceCommand()