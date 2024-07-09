package ru.kcheranev.trading.core.port.income.tradesession

interface StopTradeSessionUseCase {

    fun stopTradeSession(command: StopTradeSessionCommand)

}