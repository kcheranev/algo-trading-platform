package com.github.trading.core.port.outcome.broker

import com.github.trading.domain.model.Candle

interface HistoricCandleBrokerPort {

    fun getHistoricCandles(command: GetHistoricCandlesCommand): List<Candle>

    fun getHistoricCandlesForLongPeriod(command: GetHistoricCandlesForLongPeriodCommand): List<Candle>

    fun getLastHistoricCandles(command: GetLastHistoricCandlesCommand): List<Candle>

}