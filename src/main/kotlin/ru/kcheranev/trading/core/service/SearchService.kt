package ru.kcheranev.trading.core.service

import org.springframework.stereotype.Service
import ru.kcheranev.trading.core.mapper.commandMapper
import ru.kcheranev.trading.core.port.income.search.OrderSearchCommand
import ru.kcheranev.trading.core.port.income.search.OrderSearchUseCase
import ru.kcheranev.trading.core.port.income.search.StrategyConfigurationSearchCommand
import ru.kcheranev.trading.core.port.income.search.StrategyConfigurationSearchUseCase
import ru.kcheranev.trading.core.port.income.search.TradeSessionSearchCommand
import ru.kcheranev.trading.core.port.income.search.TradeSessionSearchUseCase
import ru.kcheranev.trading.core.port.outcome.persistence.OrderPersistencePort
import ru.kcheranev.trading.core.port.outcome.persistence.StrategyConfigurationPersistencePort
import ru.kcheranev.trading.core.port.outcome.persistence.TradeSessionPersistencePort
import ru.kcheranev.trading.domain.entity.Order
import ru.kcheranev.trading.domain.entity.StrategyConfiguration
import ru.kcheranev.trading.domain.entity.TradeSession

@Service
class SearchService(
    private val tradeSessionPersistencePort: TradeSessionPersistencePort,
    private val strategyConfigurationPersistencePort: StrategyConfigurationPersistencePort,
    private val orderPersistencePort: OrderPersistencePort
) : TradeSessionSearchUseCase,
    StrategyConfigurationSearchUseCase,
    OrderSearchUseCase {

    override fun search(command: OrderSearchCommand): List<Order> =
        orderPersistencePort.search(commandMapper.map(command))

    override fun search(command: StrategyConfigurationSearchCommand): List<StrategyConfiguration> =
        strategyConfigurationPersistencePort.search(commandMapper.map(command))

    override fun search(command: TradeSessionSearchCommand): List<TradeSession> =
        tradeSessionPersistencePort.search(commandMapper.map(command))

}