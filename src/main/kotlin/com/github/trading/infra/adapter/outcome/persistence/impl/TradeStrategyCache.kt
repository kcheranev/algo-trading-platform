package com.github.trading.infra.adapter.outcome.persistence.impl

import com.github.trading.domain.model.TradeStrategy
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class TradeStrategyCache {

    private val _tradeStrategies = ConcurrentHashMap<UUID, TradeStrategy>()

    val tradeStrategies: Map<UUID, TradeStrategy> = _tradeStrategies

    fun put(key: UUID, value: TradeStrategy) {
        _tradeStrategies[key] = value
    }

    fun get(key: UUID) = _tradeStrategies[key]

    fun computeIfAbsent(key: UUID, mappingFunction: (UUID) -> TradeStrategy) =
        _tradeStrategies.computeIfAbsent(key, mappingFunction)

    fun remove(key: UUID) = _tradeStrategies.remove(key)

    fun clear() {
        _tradeStrategies.clear()
    }

}