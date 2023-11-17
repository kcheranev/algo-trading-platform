package ru.kcheranev.trading.infra.adapter.outcome.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.kcheranev.trading.domain.entity.TradeDirection
import java.math.BigDecimal
import java.time.LocalDate

@Table(name = "order")
data class OrderEntity(
    @Id
    var id: Long? = null,
    @Column("ticker")
    val ticker: String,
    @Column("date")
    val date: LocalDate,
    @Column("quantity")
    val quantity: Int,
    @Column("price")
    val price: BigDecimal,
    @Column("direction")
    val direction: TradeDirection,
    @Column("trade_session_id")
    val tradeSessionId: Long
)
