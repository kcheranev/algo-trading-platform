package ru.kcheranev.trading.infra.adapter.outcome.persistence.impl

import org.springframework.stereotype.Component
import ru.kcheranev.trading.core.port.common.model.sort.SortDirection
import ru.kcheranev.trading.core.port.common.model.sort.TradeSessionSort
import ru.kcheranev.trading.core.port.outcome.persistence.TradeSessionSearchCommand
import ru.kcheranev.trading.domain.entity.TradeSession
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class TradeSessionCache {

    private val tradeSessions = ConcurrentHashMap<UUID, TradeSession>()

    fun get(key: UUID) = tradeSessions[key]

    fun findAll() = tradeSessions.values.toList()

    fun put(key: UUID, value: TradeSession) {
        tradeSessions[key] = value
    }

    fun remove(key: UUID) {
        tradeSessions.remove(key)
    }

    fun clear() {
        tradeSessions.clear()
    }

    fun search(command: TradeSessionSearchCommand) =
        tradeSessions.values
            .asSequence()
            .filter { if (command.id == null) true else it.id == command.id }
            .filter { if (command.ticker == null) true else it.ticker == command.ticker }
            .filter { if (command.instrumentId == null) true else it.instrumentId == command.instrumentId }
            .filter { if (command.status == null) true else it.status == command.status }
            .filter { if (command.candleInterval == null) true else it.candleInterval == command.candleInterval }
            .sortedWith { tradeSession1, tradeSession2 ->
                val sort = command.sort ?: return@sortedWith 0
                val compareResult =
                    when (sort.field) {
                        TradeSessionSort.TICKER -> tradeSession1.ticker.compareTo(tradeSession2.ticker)
                        TradeSessionSort.STATUS -> tradeSession1.status.compareTo(tradeSession2.status)
                        TradeSessionSort.START_DATE -> tradeSession1.startDate.compareTo(tradeSession2.startDate)
                        TradeSessionSort.CANDLE_INTERVAL -> tradeSession1.candleInterval.compareTo(tradeSession2.candleInterval)
                    }
                return@sortedWith if (sort.order == SortDirection.ASC) compareResult else compareResult * -1
            }
            .toList()

}