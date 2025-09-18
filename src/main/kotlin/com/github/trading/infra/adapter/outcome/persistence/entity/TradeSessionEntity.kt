package com.github.trading.infra.adapter.outcome.persistence.entity

import com.github.trading.core.strategy.lotsquantity.OrderLotsQuantityStrategyType
import com.github.trading.domain.entity.TradeSessionStatus
import com.github.trading.domain.model.CandleInterval
import com.github.trading.infra.adapter.outcome.persistence.model.MapWrapper
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.util.UUID

@Table("trade_session")
data class TradeSessionEntity(
    @Id
    val id: UUID,
    @Column("ticker")
    val ticker: String,
    @Column("instrument_id")
    val instrumentId: String,
    @Column("status")
    val status: TradeSessionStatus,
    @Column("candle_interval")
    val candleInterval: CandleInterval,
    @Column("order_lots_quantity_strategy_type")
    val orderLotsQuantityStrategyType: OrderLotsQuantityStrategyType,
    @Column("position_lots_quantity")
    val positionLotsQuantity: Int,
    @Column("position_average_price")
    val positionAveragePrice: BigDecimal,
    @Column("strategy_type")
    val strategyType: String,
    @Column("strategy_parameters")
    val strategyParameters: MapWrapper
)