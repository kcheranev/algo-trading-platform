package com.github.trading.core.port.income.tradesession

interface EnterTradeSessionUseCase {

    fun enterTradeSession(command: EnterTradeSessionCommand)

}