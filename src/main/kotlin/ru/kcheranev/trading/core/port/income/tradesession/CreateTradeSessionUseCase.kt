package ru.kcheranev.trading.core.port.income.tradesession

import ru.kcheranev.trading.domain.entity.TradeSessionId

interface CreateTradeSessionUseCase {

    fun createTradeSession(command: CreateTradeSessionCommand): TradeSessionId

}