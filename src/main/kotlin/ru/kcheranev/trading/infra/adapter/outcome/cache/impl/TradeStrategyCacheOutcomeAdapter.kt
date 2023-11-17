package ru.kcheranev.trading.infra.adapter.outcome.cache.impl

import org.springframework.stereotype.Component
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.model.TradeStrategy
import java.util.concurrent.ConcurrentHashMap

@Component
class TradeStrategyCacheOutcomeAdapter {

    private val tradeStrategies = ConcurrentHashMap<TradeSessionId, TradeStrategy>()

    fun put(tradeSessionId: TradeSessionId, tradeStrategy: TradeStrategy) =
        tradeStrategies.put(tradeSessionId, tradeStrategy)

    fun get(tradeSessionId: TradeSessionId): TradeStrategy? = tradeStrategies[tradeSessionId]

}