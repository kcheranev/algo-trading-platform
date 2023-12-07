package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom

import ru.kcheranev.trading.core.port.outcome.persistence.TradeSessionSearchCommand
import ru.kcheranev.trading.infra.adapter.outcome.persistence.entity.TradeSessionEntity

interface CustomizedTradeSessionRepository {

    fun search(command: TradeSessionSearchCommand): List<TradeSessionEntity>

}