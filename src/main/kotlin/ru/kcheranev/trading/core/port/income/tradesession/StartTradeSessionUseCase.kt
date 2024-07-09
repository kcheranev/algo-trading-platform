package ru.kcheranev.trading.core.port.income.tradesession

import ru.kcheranev.trading.domain.entity.TradeSessionId

interface StartTradeSessionUseCase {

    fun startTradeSession(command: StartTradeSessionCommand): TradeSessionId

}