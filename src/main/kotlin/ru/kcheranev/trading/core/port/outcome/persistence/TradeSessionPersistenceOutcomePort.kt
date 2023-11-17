package ru.kcheranev.trading.core.port.outcome.persistence

import ru.kcheranev.trading.domain.entity.TradeSession
import ru.kcheranev.trading.domain.entity.TradeSessionId

interface TradeSessionPersistenceOutcomePort {

    fun save(command: SaveTradeSessionPersistenceOutcomeCommand): TradeSessionId

    fun get(command: GetTradeSessionPersistenceOutcomeCommand): TradeSession

}