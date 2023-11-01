package ru.kcheranev.trading.infra.adapter.outcome.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.StrategyParameters
import ru.kcheranev.trading.domain.model.StrategyType

@Entity(name = "strategy_configuration")
data class StrategyConfiguration(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "type")
    val type: StrategyType,
    @Column(name = "init_candle_amount")
    val initCandleAmount: Int,
    @Column(name = "candle_interval")
    val candleInterval: CandleInterval,
    @Column(name = "params")
    val params: StrategyParameters
)