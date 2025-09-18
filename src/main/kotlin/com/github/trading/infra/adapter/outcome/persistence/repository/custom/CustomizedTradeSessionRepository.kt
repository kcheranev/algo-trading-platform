package com.github.trading.infra.adapter.outcome.persistence.repository.custom

import com.github.trading.core.port.outcome.persistence.tradesession.SearchTradeSessionCommand
import com.github.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity

interface CustomizedTradeSessionRepository {

    fun search(command: SearchTradeSessionCommand): List<TradeSessionEntity>

}