package com.github.trading.domain.entity.backtesting

import java.time.LocalDateTime
import java.util.UUID

data class HistoricCandleSeries(
    val id: HistoricCandleSeriesId,
    val ticker: String,
    val from: LocalDateTime,
    val to: LocalDateTime
)

data class HistoricCandleSeriesId(
    val value: UUID
) {

    override fun toString() = value.toString()

    companion object {

        fun init() = HistoricCandleSeriesId(UUID.randomUUID())

    }

}