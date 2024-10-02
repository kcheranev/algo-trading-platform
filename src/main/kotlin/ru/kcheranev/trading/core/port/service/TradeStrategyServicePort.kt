package ru.kcheranev.trading.core.port.service

import ru.kcheranev.trading.core.port.service.command.InitTradeStrategyCommand
import ru.kcheranev.trading.domain.model.TradeStrategy

interface TradeStrategyServicePort {

    fun initTradeStrategy(command: InitTradeStrategyCommand): TradeStrategy

}