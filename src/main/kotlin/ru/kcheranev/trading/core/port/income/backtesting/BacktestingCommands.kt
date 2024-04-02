package ru.kcheranev.trading.core.port.income.backtesting

import ru.kcheranev.trading.domain.model.CandleInterval
import ru.kcheranev.trading.domain.model.Instrument
import java.time.LocalDateTime

sealed class BacktestingCommand

data class StrategyAnalyzeCommand(
    val strategyType: String,
    val strategyParams: Map<String, Any>,
    val instrument: Instrument,
    val candleInterval: CandleInterval,
    val candlesFrom: LocalDateTime,
    val candlesTo: LocalDateTime
) : BacktestingCommand()