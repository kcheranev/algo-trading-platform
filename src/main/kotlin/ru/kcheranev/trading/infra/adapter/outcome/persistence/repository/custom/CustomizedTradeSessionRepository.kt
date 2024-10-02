package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom

import ru.kcheranev.trading.core.port.outcome.persistence.tradesession.SearchTradeSessionCommand
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity

interface CustomizedTradeSessionRepository {

    fun search(command: SearchTradeSessionCommand): List<TradeSessionEntity>

}