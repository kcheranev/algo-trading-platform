package com.github.trading.core.port.income.tradesession

interface ExitTradeSessionUseCase {

    fun exitTradeSession(command: ExitTradeSessionCommand)

}