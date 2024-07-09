package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom

import ru.kcheranev.trading.core.port.outcome.persistence.tradeorder.SearchTradeOrderCommand
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeOrderEntity

interface CustomizedTradeOrderRepository {

    fun search(command: SearchTradeOrderCommand): List<TradeOrderEntity>

}