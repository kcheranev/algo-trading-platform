package ru.kcheranev.trading.core.service

import org.springframework.stereotype.Service
import ru.kcheranev.trading.core.port.income.tradeorder.SearchTradeOrderCommand
import ru.kcheranev.trading.core.port.income.tradeorder.SearchTradeOrderUseCase
import ru.kcheranev.trading.core.port.mapper.commandMapper
import ru.kcheranev.trading.core.port.outcome.persistence.tradeorder.TradeOrderPersistencePort

@Service
class TradeOrderService(
    private val tradeOrderPersistencePort: TradeOrderPersistencePort
) : SearchTradeOrderUseCase {

    override fun search(command: SearchTradeOrderCommand) =
        tradeOrderPersistencePort.search(commandMapper.map(command))

}