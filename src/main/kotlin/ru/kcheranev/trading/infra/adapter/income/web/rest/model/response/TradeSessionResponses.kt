package ru.kcheranev.trading.infra.adapter.income.web.rest.model.response

import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import java.util.UUID

data class TradeSessionDto(
    var id: UUID,
    var ticker: String,
    var instrumentId: String,
    var status: TradeSessionStatus,
    var candleInterval: CandleInterval,
    var lotsQuantity: Int,
    val lotsQuantityInPosition: Int,
    var strategyType: String,
    val strategyParameters: Map<String, Number>
)

data class CreateTradeSessionResponseDto(
    val tradeSessionId: UUID
)

data class TradeSessionSearchResponseDto(
    var tradeSessions: List<TradeSessionDto>
)