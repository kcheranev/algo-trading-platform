package com.github.trading.core.port.income.tradesession

import com.github.trading.domain.model.view.TradeSessionView

interface SearchTradeSessionUseCase {

    fun search(command: SearchTradeSessionCommand): List<TradeSessionView>

}