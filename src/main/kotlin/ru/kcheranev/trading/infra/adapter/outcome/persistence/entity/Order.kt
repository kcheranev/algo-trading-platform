package ru.kcheranev.trading.infra.adapter.outcome.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import ru.kcheranev.trading.domain.entity.TradeDirection
import java.math.BigDecimal
import java.time.LocalDate

@Entity(name = "order")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "date")
    val date: LocalDate,
    @Column(name = "quantity")
    val quantity: Int,
    @Column(name = "price")
    val price: BigDecimal,
    @Column(name = "direction")
    val direction: TradeDirection,
    @ManyToOne
    val tradeSession: TradeSession
)
