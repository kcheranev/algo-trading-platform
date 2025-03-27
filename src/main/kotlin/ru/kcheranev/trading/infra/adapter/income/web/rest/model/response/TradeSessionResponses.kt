package ru.kcheranev.trading.infra.adapter.income.web.rest.model.response

import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import java.math.BigDecimal
import java.util.UUID

data class TradeSessionDto(
    val id: UUID,
    val ticker: String,
    val instrumentId: String,
    val status: TradeSessionStatus,
    val candleInterval: CandleInterval,
    val lotsQuantity: Int,
    val currentPosition: CurrentPositionDto,
    val strategyType: String,
    val strategyParameters: Map<String, Number>
)

data class CurrentPositionDto(
    val lotsQuantity: Int,
    val averagePrice: BigDecimal
)

data class CreateTradeSessionResponseDto(
    val tradeSessionId: UUID
)

data class TradeSessionSearchResponseDto(
    val tradeSessions: List<TradeSessionDto>
)