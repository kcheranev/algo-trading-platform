package com.github.trading.infra.adapter.outcome.persistence.repository.custom

import com.github.trading.core.port.outcome.persistence.tradeorder.SearchTradeOrderCommand
import com.github.trading.infra.adapter.outcome.persistence.entity.TradeOrderEntity

interface CustomizedTradeOrderRepository {

    fun search(command: SearchTradeOrderCommand): List<TradeOrderEntity>

}