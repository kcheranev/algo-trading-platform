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
    val id: UUID,
    @Column("name")
    val name: String,
    @Column("type")
    val type: String,
    @Column("candle_interval")
    val candleInterval: CandleInterval,
    @Column("parameters")
    val parameters: MapWrapper<String>
)