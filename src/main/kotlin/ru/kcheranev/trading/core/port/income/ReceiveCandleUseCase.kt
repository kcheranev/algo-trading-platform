package ru.kcheranev.trading.core.port.income

interface ReceiveCandleUseCase {

    fun processIncomeCandle(command: ProcessIncomeCandleCommand)

}