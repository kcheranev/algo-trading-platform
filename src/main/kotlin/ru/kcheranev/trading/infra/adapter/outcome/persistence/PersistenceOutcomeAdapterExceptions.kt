package ru.kcheranev.trading.infra.adapter.outcome.persistence

import ru.kcheranev.trading.domain.entity.OrderId
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.entity.TradeSessionId

abstract class PersistenceOutcomeAdapterException(
    message: String
) : RuntimeException(message)

class TradeSessionEntityNotExistsException(tradeSessionId: TradeSessionId) :
    PersistenceOutcomeAdapterException("Trade session entity with id ${tradeSessionId.value} is not exists")

class TradeStrategyCacheNotExistsException(tradeSessionId: Long) :
    PersistenceOutcomeAdapterException("Trade strategy cache with key $tradeSessionId is not exists")

class OrderEntityNotExistsException(orderId: OrderId) :
    PersistenceOutcomeAdapterException("Order entity with id ${orderId.value} is not exists")

class StrategyConfigurationEntityNotExistsException(strategyConfigurationId: StrategyConfigurationId) :
    PersistenceOutcomeAdapterException("Strategy configuration entity with id ${strategyConfigurationId.value} is not exists")