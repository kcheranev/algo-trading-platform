package com.github.trading.core.port.outcome.persistence.tradesession

import com.github.trading.core.port.model.Page
import com.github.trading.core.port.model.sort.Sort
import com.github.trading.core.port.model.sort.TradeSessionSort
import com.github.trading.domain.entity.TradeSession
import com.github.trading.domain.entity.TradeSessionId
import com.github.trading.domain.entity.TradeSessionStatus
import com.github.trading.domain.model.CandleInterval

data class InsertTradeSessionCommand(
    val tradeSession: TradeSession
)

data class SaveTradeSessionCommand(
    val tradeSession: TradeSession
)

data class GetTradeSessionCommand(
    val tradeSessionId: TradeSessionId
)

data class GetReadyToOrderTradeSessionsCommand(
    val instrumentId: String,
    val candleInterval: CandleInterval
)

data class IsReadyToOrderTradeSessionExistsCommand(
    val instrumentId: String,
    val candleInterval: CandleInterval
)

data class SearchTradeSessionCommand(
    val id: TradeSessionId?,
    val ticker: String?,
    val instrumentId: String?,
    val status: TradeSessionStatus?,
    val candleInterval: CandleInterval?,
    val page: Page? = null,
    val sort: Sort<TradeSessionSort>?
)