package ru.kcheranev.trading.infra.adapter.outcome.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table(name = "instrument")
data class InstrumentEntity(
    @Id
    var id: UUID,
    @Column("ticker")
    val ticker: String,
    @Column("instrument_id")
    val instrumentId: String
)