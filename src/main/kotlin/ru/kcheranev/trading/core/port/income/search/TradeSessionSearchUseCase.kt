package ru.kcheranev.trading.core.port.income.search

import ru.kcheranev.trading.domain.entity.TradeSession

interface TradeSessionSearchUseCase {

    fun search(command: TradeSessionSearchCommand): List<TradeSession>

}