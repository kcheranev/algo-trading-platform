package ru.kcheranev.trading.core.port.income.tradesession

import ru.kcheranev.trading.domain.entity.TradeSession

interface SearchTradeSessionUseCase {

    fun search(command: SearchTradeSessionCommand): List<TradeSession>

}