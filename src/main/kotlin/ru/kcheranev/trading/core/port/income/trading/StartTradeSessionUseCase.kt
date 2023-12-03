package ru.kcheranev.trading.core.port.income.trading

interface StartTradeSessionUseCase {

    fun startTradeSession(command: StartTradeSessionCommand)

}