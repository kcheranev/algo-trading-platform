package ru.kcheranev.trading.core.port.income.trading

import ru.kcheranev.trading.domain.entity.TradeSessionId

interface StartTradeSessionUseCase {

    fun startTradeSession(command: StartTradeSessionCommand): TradeSessionId

}