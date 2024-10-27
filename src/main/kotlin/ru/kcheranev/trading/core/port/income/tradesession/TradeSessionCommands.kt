package ru.kcheranev.trading.core.port.income.tradesession

import ru.kcheranev.trading.common.Default
import ru.kcheranev.trading.core.port.model.Page
import ru.kcheranev.trading.core.port.model.sort.Sort
import ru.kcheranev.trading.core.port.model.sort.TradeSessionSort
import ru.kcheranev.trading.domain.entity.StrategyConfigurationId
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument

data class CreateTradeSessionCommand(
    val strategyConfigurationId: StrategyConfigurationId,
    val lotsQuantity: Int,
    val instrument: Instrument
)

data class EnterTradeSessionCommand(
    val tradeSessionId: TradeSessionId
)

data class ExitTradeSessionCommand(
    val tradeSessionId: TradeSessionId
)

data class StopTradeSessionCommand(
    val tradeSessionId: TradeSessionId
)

data class ResumeTradeSessionCommand(
    val tradeSessionId: TradeSessionId
)

data class SearchTradeSessionCommand @Default constructor(
    val id: TradeSessionId? = null,
    val ticker: String? = null,
    val instrumentId: String? = null,
    val status: TradeSessionStatus? = null,
    val candleInterval: CandleInterval? = null,
    val page: Page? = null,
    val sort: Sort<TradeSessionSort>? = null
)