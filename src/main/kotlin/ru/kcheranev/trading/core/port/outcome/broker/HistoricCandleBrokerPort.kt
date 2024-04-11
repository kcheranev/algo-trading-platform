package ru.kcheranev.trading.core.port.outcome.broker

import ru.kcheranev.trading.domain.model.Candle
import java.time.LocalDate

interface HistoricCandleBrokerPort {

    fun getHistoricCandles(command: GetHistoricCandlesCommand): List<Candle>

    fun getHistoricCandlesForLongPeriod(command: GetHistoricCandlesForLongPeriodCommand): Map<LocalDate, List<Candle>>

    fun getLastHistoricCandles(command: GetLastHistoricCandlesCommand): List<Candle>

}