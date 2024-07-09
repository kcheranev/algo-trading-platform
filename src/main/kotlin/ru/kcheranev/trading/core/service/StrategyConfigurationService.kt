package ru.kcheranev.trading.core.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.kcheranev.trading.core.port.income.strategyconfiguration.CreateStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.income.strategyconfiguration.CreateStrategyConfigurationUseCase
import ru.kcheranev.trading.core.port.income.strategyconfiguration.SearchStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.income.strategyconfiguration.SearchStrategyConfigurationUseCase
import ru.kcheranev.trading.core.port.mapper.commandMapper
import ru.kcheranev.trading.core.port.outcome.persistence.strategyconfiguration.SaveStrategyConfigurationCommand
import ru.kcheranev.trading.core.port.outcome.persistence.strategyconfiguration.StrategyConfigurationPersistencePort
import ru.kcheranev.trading.domain.entity.StrategyConfiguration

@Service
class StrategyConfigurationService(
    private val strategyConfigurationPersistencePort: StrategyConfigurationPersistencePort
) : CreateStrategyConfigurationUseCase,
    SearchStrategyConfigurationUseCase {

    @Transactional
    override fun createStrategyConfiguration(command: CreateStrategyConfigurationCommand) {
        val strategyConfiguration =
            StrategyConfiguration.create(
                type = command.type,
                candleInterval = command.candleInterval,
                params = command.params
            )
        strategyConfigurationPersistencePort.save(SaveStrategyConfigurationCommand(strategyConfiguration))
    }

    override fun search(command: SearchStrategyConfigurationCommand) =
        strategyConfigurationPersistencePort.search(commandMapper.map(command))

}