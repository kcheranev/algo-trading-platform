package ru.kcheranev.trading.core.port.income.trading

interface ReceiveCandleUseCase {

    fun processIncomeCandle(command: ProcessIncomeCandleCommand)

}