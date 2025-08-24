package ru.kcheranev.trading.domain.model.view

import ru.kcheranev.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyType
import ru.kcheranev.trading.domain.entity.TradeSessionId
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyParameters
import java.math.BigDecimal

data class TradeSessionView(
    val id: TradeSessionId,
    val ticker: String,
    val instrumentId: String,
    val status: TradeSessionStatus,
    val candleInterval: CandleInterval,
    val orderLotsQuantityStrategyType: OrderLotsQuantityStrategyType,
    val currentPosition: CurrentPositionView,
    val strategyType: String,
    val strategyParameters: StrategyParameters
)

data class CurrentPositionView(
    val lotsQuantity: Int,
    val averagePrice: BigDecimal
)