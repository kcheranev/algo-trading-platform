package ru.kcheranev.trading.infra.adapter.outcome.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.kcheranev.trading.domain.entity.OrderDirection
import java.math.BigDecimal
import java.time.LocalDateTime

@Table(name = "order")
data class OrderEntity(
    @Id
    var id: Long? = null,
    @Column("ticker")
    val ticker: String,
    @Column("instrument_id")
    val instrumentId: String,
    @Column("date")
    val date: LocalDateTime,
    @Column("lots_quantity")
    val lotsQuantity: Int,
    @Column("price")
    val price: BigDecimal,
    @Column("direction")
    val direction: OrderDirection,
    @Column("trade_session_id")
    val tradeSessionId: Long
)
