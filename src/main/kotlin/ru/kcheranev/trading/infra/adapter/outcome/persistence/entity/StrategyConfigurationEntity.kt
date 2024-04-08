package ru.kcheranev.trading.infra.adapter.outcome.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper
import java.util.UUID

@Table("strategy_configuration")
data class StrategyConfigurationEntity(
    @Id
    var id: UUID? = null,
    @Column("type")
    val type: String,
    @Column("init_candle_amount")
    val initCandleAmount: Int,
    @Column("candle_interval")
    val candleInterval: CandleInterval,
    @Column("params")
    val params: MapWrapper<String, Int>
)