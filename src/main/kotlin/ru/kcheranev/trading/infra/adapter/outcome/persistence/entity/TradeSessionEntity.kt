package ru.kcheranev.trading.infra.adapter.outcome.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import java.time.LocalDateTime
import java.util.UUID

@Table("trade_session")
data class TradeSessionEntity(
    @Id
    var id: UUID,
    @Column("ticker")
    val ticker: String,
    @Column("instrument_id")
    val instrumentId: String,
    @Column("status")
    val status: TradeSessionStatus,
    @Column("start_date")
    val startDate: LocalDateTime,
    @Column("candle_interval")
    val candleInterval: CandleInterval,
    @Column("lots_quantity")
    val lotsQuantity: Int,
    @Column("lots_quantity_in_position")
    val lotsQuantityInPosition: Int,
    @Column("strategy_type")
    val strategyType: String,
    @Column("strategy_parameters")
    val strategyParameters: MapWrapper<String>
)