package com.github.trading.domain.entity.backtesting

import java.time.LocalDateTime
import java.util.UUID

data class BacktestingTask(
    val id: BacktestingTaskId,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val status: BacktestingTaskStatus,
    val candleSeries: List<HistoricCandleSeries>
)

data class BacktestingTaskId(
    val value: UUID
) {

    override fun toString() = value.toString()

    companion object {

        fun init() = BacktestingTaskId(UUID.randomUUID())

    }

}

enum class BacktestingTaskStatus {

    NEW, IN_PROCESS, FINISHED

}