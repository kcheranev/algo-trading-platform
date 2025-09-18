package com.github.trading.core.port.income.tradesession

import com.github.trading.common.Default
import com.github.trading.core.port.model.Page
import com.github.trading.core.port.model.sort.Sort
import com.github.trading.core.port.model.sort.TradeSessionSort
import com.github.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyType
import com.github.trading.domain.entity.StrategyConfigurationId
import com.github.trading.domain.entity.TradeSessionId
import com.github.trading.domain.entity.TradeSessionStatus
import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.Instrument

data class CreateTradeSessionCommand(
    val strategyConfigurationId: StrategyConfigurationId,
    val orderLotsQuantityStrategyType: OrderLotsQuantityStrategyType,
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