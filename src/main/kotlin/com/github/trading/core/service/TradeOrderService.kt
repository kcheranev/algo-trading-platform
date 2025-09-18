package com.github.trading.core.service

import com.github.trading.core.port.income.tradeorder.SearchTradeOrderCommand
import com.github.trading.core.port.income.tradeorder.SearchTradeOrderUseCase
import com.github.trading.core.port.mapper.commandMapper
import com.github.trading.core.port.outcome.persistence.tradeorder.TradeOrderPersistencePort
import org.springframework.stereotype.Service

@Service
class TradeOrderService(
    private val tradeOrderPersistencePort: TradeOrderPersistencePort
) : SearchTradeOrderUseCase {

    override fun search(command: SearchTradeOrderCommand) =
        tradeOrderPersistencePort.search(commandMapper.map(command))

}