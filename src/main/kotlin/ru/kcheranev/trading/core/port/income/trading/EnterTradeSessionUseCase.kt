package ru.kcheranev.trading.core.port.income.trading

interface EnterTradeSessionUseCase {

    fun enterTradeSession(command: EnterTradeSessionCommand)

}