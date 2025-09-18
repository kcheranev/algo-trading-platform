package com.github.trading.core.port.outcome.persistence.tradesession

import com.github.trading.domain.entity.TradeSession
import com.github.trading.domain.model.view.TradeSessionView

interface TradeSessionPersistencePort {

    fun insert(command: InsertTradeSessionCommand)

    fun save(command: SaveTradeSessionCommand)

    fun get(command: GetTradeSessionCommand): TradeSession

    fun search(command: SearchTradeSessionCommand): List<TradeSessionView>

    fun getReadyForOrderTradeSessions(): List<TradeSession>

    fun getReadyForOrderTradeSessions(command: GetReadyToOrderTradeSessionsCommand): List<TradeSession>

    fun isReadyForOrderTradeSessionExists(command: IsReadyToOrderTradeSessionExistsCommand): Boolean

}