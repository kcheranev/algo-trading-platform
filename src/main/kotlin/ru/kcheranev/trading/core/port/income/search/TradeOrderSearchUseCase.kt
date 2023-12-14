package ru.kcheranev.trading.core.port.income.search

import ru.kcheranev.trading.domain.entity.TradeOrder

interface TradeOrderSearchUseCase {

    fun search(command: TradeOrderSearchCommand): List<TradeOrder>

}