package com.github.trading.core.service

import com.github.trading.core.port.income.strategyconfiguration.CreateStrategyConfigurationCommand
import com.github.trading.core.port.income.strategyconfiguration.CreateStrategyConfigurationUseCase
import com.github.trading.core.port.income.strategyconfiguration.SearchStrategyConfigurationCommand
import com.github.trading.core.port.income.strategyconfiguration.SearchStrategyConfigurationUseCase
import com.github.trading.core.port.mapper.commandMapper
import com.github.trading.core.port.outcome.persistence.strategyconfiguration.InsertStrategyConfigurationCommand
import com.github.trading.core.port.outcome.persistence.strategyconfiguration.StrategyConfigurationPersistencePort
import com.github.trading.domain.entity.StrategyConfiguration
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StrategyConfigurationService(
    private val strategyConfigurationPersistencePort: StrategyConfigurationPersistencePort
) : CreateStrategyConfigurationUseCase,
    SearchStrategyConfigurationUseCase {

    @Transactional
    override fun createStrategyConfiguration(command: CreateStrategyConfigurationCommand) {
        val strategyConfiguration =
            StrategyConfiguration.create(
                name = command.name,
                type = command.type,
                candleInterval = command.candleInterval,
                parameters = command.parameters
            )
        strategyConfigurationPersistencePort.insert(InsertStrategyConfigurationCommand(strategyConfiguration))
    }

    override fun search(command: SearchStrategyConfigurationCommand) =
        strategyConfigurationPersistencePort.search(commandMapper.map(command))

}