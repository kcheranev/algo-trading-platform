package ru.kcheranev.trading.core.port.outcome.persistence.tradesession

import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.model.view.TradeSessionView

interface TradeSessionPersistencePort {

    fun insert(command: InsertTradeSessionCommand): TradeSessionId

    fun save(command: SaveTradeSessionCommand): TradeSessionId

    fun get(command: GetTradeSessionCommand): TradeSession

    fun search(command: SearchTradeSessionCommand): List<TradeSessionView>

    fun getReadyForOrderTradeSessions(command: GetReadyToOrderTradeSessionsCommand): List<TradeSession>

}