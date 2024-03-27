package ru.kcheranev.trading.core.service

import org.springframework.stereotype.Service
import ru.kcheranev.trading.core.mapper.commandMapper
import ru.kcheranev.trading.core.port.income.search.StrategyConfigurationSearchCommand
import ru.kcheranev.trading.core.port.income.search.StrategyConfigurationSearchUseCase
import ru.kcheranev.trading.core.port.income.search.TradeOrderSearchCommand
import ru.kcheranev.trading.core.port.income.search.TradeOrderSearchUseCase
import ru.kcheranev.trading.core.port.income.search.TradeSessionSearchCommand
import ru.kcheranev.trading.core.port.income.search.TradeSessionSearchUseCase
import ru.kcheranev.trading.core.port.outcome.persistence.StrategyConfigurationPersistencePort
import ru.kcheranev.trading.core.port.outcome.persistence.TradeOrderPersistencePort
import ru.kcheranev.trading.core.port.outcome.persistence.TradeSessionPersistencePort

@Service
class SearchService(
    private val tradeSessionPersistencePort: TradeSessionPersistencePort,
    private val strategyConfigurationPersistencePort: StrategyConfigurationPersistencePort,
    private val tradeOrderPersistencePort: TradeOrderPersistencePort
) : TradeSessionSearchUseCase,
    StrategyConfigurationSearchUseCase,
    TradeOrderSearchUseCase {

    override fun search(command: TradeOrderSearchCommand) =
        tradeOrderPersistencePort.search(commandMapper.map(command))

    override fun search(command: StrategyConfigurationSearchCommand) =
        strategyConfigurationPersistencePort.search(commandMapper.map(command))

    override fun search(command: TradeSessionSearchCommand) =
        tradeSessionPersistencePort.search(commandMapper.map(command))

}