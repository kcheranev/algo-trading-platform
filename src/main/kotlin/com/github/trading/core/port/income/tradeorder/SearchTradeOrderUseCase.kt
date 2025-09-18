package com.github.trading.core.port.income.tradeorder

import com.github.trading.domain.entity.TradeOrder

interface SearchTradeOrderUseCase {

    fun search(command: SearchTradeOrderCommand): List<TradeOrder>

}