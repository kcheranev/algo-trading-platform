package ru.kcheranev.trading.core.port.income

interface StartTradeSessionUseCase {

    fun startTradeSession(command: StartTradeSessionCommand)

}