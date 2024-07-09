package ru.kcheranev.trading.core.port.outcome.persistence.tradesession

import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId

interface TradeSessionPersistencePort {

    fun save(command: SaveTradeSessionCommand): TradeSessionId

    fun get(command: GetTradeSessionCommand): TradeSession

    fun search(command: SearchTradeSessionCommand): List<TradeSession>

    fun getReadyToOrderTradeSessions(command: GetReadyToOrderTradeSessionsCommand): List<TradeSession>

}