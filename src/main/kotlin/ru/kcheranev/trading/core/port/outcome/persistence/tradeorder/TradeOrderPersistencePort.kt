package ru.kcheranev.trading.core.port.outcome.persistence.tradeorder

import ru.kcheranev.trading.domain.entity.TradeOrder

interface TradeOrderPersistencePort {

    fun insert(command: InsertTradeOrderCommand)

    fun get(command: GetTradeOrderCommand): TradeOrder

    fun search(command: SearchTradeOrderCommand): List<TradeOrder>

}