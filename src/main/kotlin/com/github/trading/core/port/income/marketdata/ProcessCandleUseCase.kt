package com.github.trading.core.port.income.marketdata

interface ProcessCandleUseCase {

    fun processIncomeCandle(command: ProcessIncomeCandleCommand)

}