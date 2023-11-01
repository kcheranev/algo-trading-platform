package ru.kcheranev.trading.infra.adapter.outcome.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import ru.kcheranev.trading.domain.TradeSessionStatus
import java.time.LocalDate

@Entity(name = "trade_session")
data class TradeSession(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "ticker")
    val ticker: String,
    @Column(name = "status")
    val status: TradeSessionStatus,
    @Column(name = "start_date")
    val startDate: LocalDate,
    @Column(name = "last_event_date")
    val lastEventDate: LocalDate,
    @ManyToOne
    val configuration: StrategyConfiguration
)