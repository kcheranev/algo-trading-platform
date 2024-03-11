package ru.kcheranev.trading.infra.adapter.outcome.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import java.time.LocalDateTime

@Table("trade_session")
data class TradeSessionEntity(
    @Id
    var id: Long? = null,
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
    @Column("strategy_configuration_id")
    val strategyConfigurationId: Long
)