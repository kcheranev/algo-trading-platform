package com.github.trading.core.port.income.tradesession

interface ResumeTradeSessionUseCase {

    fun resumeTradeSession(command: ResumeTradeSessionCommand)

}