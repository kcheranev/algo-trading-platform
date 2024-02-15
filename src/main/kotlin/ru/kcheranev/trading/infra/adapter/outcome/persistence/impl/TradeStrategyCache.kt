package ru.kcheranev.trading.infra.adapter.outcome.persistence.impl

import org.springframework.stereotype.Component
import ru.kcheranev.trading.domain.model.TradeStrategy
import ru.kcheranev.trading.infra.adapter.outcome.persistence.TradeStrategyCacheNotExistsException
import java.util.concurrent.ConcurrentHashMap

@Component
class TradeStrategyCache {

    private val tradeStrategies = ConcurrentHashMap<Long, TradeStrategy>()

    fun get(key: Long) = tradeStrategies[key] ?: throw TradeStrategyCacheNotExistsException(key)

    fun put(key: Long, value: TradeStrategy) {
        tradeStrategies[key] = value
    }

    fun contains(key: Long) = tradeStrategies.containsKey(key)

    fun clear() {
        tradeStrategies.clear()
    }

}