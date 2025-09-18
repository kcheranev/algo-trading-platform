package com.github.trading.domain.model.view

import com.github.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyType
import com.github.trading.domain.entity.TradeSessionId
import com.github.trading.domain.entity.TradeSessionStatus
import com.github.trading.domain.model.CandleInterval
import com.github.trading.domain.model.StrategyParameters
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