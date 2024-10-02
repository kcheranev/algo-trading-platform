package ru.kcheranev.trading.core.port.outcome.persistence.tradeorder

import ru.kcheranev.trading.domain.entity.TradeOrder
import ru.kcheranev.trading.domain.entity.TradeOrderId

interface TradeOrderPersistencePort {

    fun insert(command: InsertTradeOrderCommand): TradeOrderId

    fun get(command: GetTradeOrderCommand): TradeOrder

    fun search(command: SearchTradeOrderCommand): List<TradeOrder>

}