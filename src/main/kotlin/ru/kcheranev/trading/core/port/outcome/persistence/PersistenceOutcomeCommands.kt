package ru.kcheranev.trading.core.port.outcome.persistence

import ru.kcheranev.trading.domain.entity.Order
import ru.kcheranev.trading.domain.entity.OrderId
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId

sealed class PersistenceOutcomeCommand

data class SaveOrderPersistenceOutcomeCommand(
    val order: Order
) : PersistenceOutcomeCommand()

data class GetOrderPersistenceOutcomeCommand(
    val orderId: OrderId
) : PersistenceOutcomeCommand()

data class SaveTradeSessionPersistenceOutcomeCommand(
    val tradeSession: TradeSession
) : PersistenceOutcomeCommand()

data class GetTradeSessionPersistenceOutcomeCommand(
    val tradeSessionId: TradeSessionId
) : PersistenceOutcomeCommand()

data class SaveStrategyConfigurationPersistenceOutcomeCommand(
    val strategyConfiguration: StrategyConfiguration
) : PersistenceOutcomeCommand()

data class GetStrategyConfigurationPersistenceOutcomeCommand(
    val strategyConfigurationId: StrategyConfigurationId
) : PersistenceOutcomeCommand()