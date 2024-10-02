package ru.kcheranev.trading.core.port.income.tradesession

import ru.kcheranev.trading.domain.model.view.TradeSessionView

interface SearchTradeSessionUseCase {

    fun search(command: SearchTradeSessionCommand): List<TradeSessionView>

}