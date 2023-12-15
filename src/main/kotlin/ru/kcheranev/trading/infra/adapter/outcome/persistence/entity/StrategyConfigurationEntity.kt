package ru.kcheranev.trading.infra.adapter.outcome.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyType
import ru.kcheranev.trading.infra.adapter.outcome.persistence.model.MapWrapper

@Table("strategy_configuration")
data class StrategyConfigurationEntity(
    @Id
    var id: Long? = null,
    @Column("type")
    val type: StrategyType,
    @Column("init_candle_amount")
    val initCandleAmount: Int,
    @Column("candle_interval")
    val candleInterval: CandleInterval,
    @Column("params")
    val params: MapWrapper<String, Any>
)