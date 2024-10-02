package ru.kcheranev.trading.domain.model.view

import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyParameters
import java.time.LocalDateTime

data class TradeSessionView(
    val id: TradeSessionId,
    val ticker: String,
    val instrumentId: String,
    var status: TradeSessionStatus,
    val startDate: LocalDateTime,
    val candleInterval: CandleInterval,
    val lotsQuantity: Int,
    var lotsQuantityInPosition: Int,
    val strategyType: String,
    val strategyParameters: StrategyParameters
)