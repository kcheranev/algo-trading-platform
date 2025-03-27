package ru.kcheranev.trading.infra.adapter.outcome.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.kcheranev.trading.domain.model.TradeDirection
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Table(name = "trade_order")
data class TradeOrderEntity(
    @Id
    val id: UUID,
    @Column("ticker")
    val ticker: String,
    @Column("instrument_id")
    val instrumentId: String,
    @Column("date")
    val date: LocalDateTime,
    @Column("lots_quantity")
    val lotsQuantity: Int,
    @Column("total_price")
    val totalPrice: BigDecimal,
    @Column("executed_commission")
    val executedCommission: BigDecimal,
    @Column("direction")
    val direction: TradeDirection,
    @Column("trade_session_id")
    val tradeSessionId: UUID
)