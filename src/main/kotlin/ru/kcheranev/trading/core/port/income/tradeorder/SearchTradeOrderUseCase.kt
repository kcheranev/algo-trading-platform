package ru.kcheranev.trading.core.port.income.tradeorder

import ru.kcheranev.trading.domain.entity.TradeOrder

interface SearchTradeOrderUseCase {

    fun search(command: SearchTradeOrderCommand): List<TradeOrder>

}