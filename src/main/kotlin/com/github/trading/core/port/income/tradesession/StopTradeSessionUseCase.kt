package com.github.trading.core.port.income.tradesession

interface StopTradeSessionUseCase {

    fun stopTradeSession(command: StopTradeSessionCommand)

}