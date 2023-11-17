package ru.kcheranev.trading.infra.adapter.outcome.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.kcheranev.trading.domain.entity.TradeSessionStatus
import ru.kcheranev.trading.domain.model.CandleInterval
import java.time.LocalDate

@Table("trade_session")
data class TradeSessionEntity(
    @Id
    var id: Long? = null,
    @Column("ticker")
    val ticker: String,
    @Column("status")
    val status: TradeSessionStatus,
    @Column("start_date")
    val startDate: LocalDate,
    @Column("candle_interval")
    val candleInterval: CandleInterval,
    @Column("last_event_date")
    val lastEventDate: LocalDate,
    @Column("strategy_configuration_id")
    val strategyConfigurationId: Long
)