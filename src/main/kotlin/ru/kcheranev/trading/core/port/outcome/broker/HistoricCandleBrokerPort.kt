package ru.kcheranev.trading.core.port.outcome.broker

import ru.kcheranev.trading.domain.model.Candle

interface HistoricCandleBrokerPort {

    fun getHistoricCandles(command: GetHistoricCandlesCommand): List<Candle>

    fun getLastHistoricCandles(command: GetLastHistoricCandlesCommand): List<Candle>

}