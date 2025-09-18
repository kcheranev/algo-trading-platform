package com.github.trading.core.port.income.tradesession

import com.github.trading.domain.entity.TradeSessionId

interface CreateTradeSessionUseCase {

    fun createTradeSession(command: CreateTradeSessionCommand): TradeSessionId

}