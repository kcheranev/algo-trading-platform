package com.github.trading.core.port.service

import com.github.trading.core.port.service.command.InitTradeStrategyCommand
import com.github.trading.domain.model.TradeStrategy

interface TradeStrategyServicePort {

    fun initTradeStrategy(command: InitTradeStrategyCommand): TradeStrategy

}