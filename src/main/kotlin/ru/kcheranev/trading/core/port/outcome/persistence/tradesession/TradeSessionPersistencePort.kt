package ru.kcheranev.trading.core.port.outcome.persistence.tradesession

import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.model.view.TradeSessionView

interface TradeSessionPersistencePort {

    fun insert(command: InsertTradeSessionCommand)

    fun save(command: SaveTradeSessionCommand)

    fun get(command: GetTradeSessionCommand): TradeSession

    fun search(command: SearchTradeSessionCommand): List<TradeSessionView>

    fun getReadyForOrderTradeSessions(): List<TradeSession>

    fun getReadyForOrderTradeSessions(command: GetReadyToOrderTradeSessionsCommand): List<TradeSession>

    fun isReadyForOrderTradeSessionExists(command: IsReadyToOrderTradeSessionExistsCommand): Boolean

}