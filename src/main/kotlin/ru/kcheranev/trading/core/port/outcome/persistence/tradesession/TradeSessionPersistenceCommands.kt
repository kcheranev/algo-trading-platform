package ru.kcheranev.trading.core.port.outcome.persistence.tradesession

import ru.kcheranev.trading.core.port.model.Page
import ru.kcheranev.trading.core.port.model.sort.Sort
import ru.kcheranev.trading.core.port.model.sort.TradeSessionSort
import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval

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