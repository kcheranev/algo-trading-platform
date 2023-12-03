package ru.kcheranev.trading.core.port.income.trading

interface ExitTradeSessionUseCase {

    fun exitTradeSession(command: ExitTradeSessionCommand)

}