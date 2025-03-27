package ru.kcheranev.trading.infra.adapter.outcome.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
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
    @Column("lots_quantity")
    val lotsQuantity: Int,
    @Column("position_lots_quantity")
    val positionLotsQuantity: Int,
    @Column("position_average_price")
    val positionAveragePrice: BigDecimal,
    @Column("strategy_type")
    val strategyType: String,
    @Column("strategy_parameters")
    val strategyParameters: MapWrapper<String>
)