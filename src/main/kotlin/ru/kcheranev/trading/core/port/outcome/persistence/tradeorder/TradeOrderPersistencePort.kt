package ru.kcheranev.trading.core.port.outcome.persistence.tradeorder

import ru.kcheranev.trading.domain.entity.TradeOrder
import ru.kcheranev.trading.domain.entity.TradeOrderId

interface TradeOrderPersistencePort {

    fun save(command: SaveOrderCommand): TradeOrderId

    fun get(command: GetOrderCommand): TradeOrder

    fun search(command: SearchTradeOrderCommand): List<TradeOrder>

}