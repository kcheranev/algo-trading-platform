package ru.kcheranev.trading.core.port.income.marketdata

interface ProcessCandleUseCase {

    fun processIncomeCandle(command: ProcessIncomeCandleCommand)

}