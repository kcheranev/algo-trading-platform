package ru.kcheranev.trading.core.port.income.trading

interface StopTradeSessionUseCase {

    fun stopTradeSession(command: StopTradeSessionCommand)

}