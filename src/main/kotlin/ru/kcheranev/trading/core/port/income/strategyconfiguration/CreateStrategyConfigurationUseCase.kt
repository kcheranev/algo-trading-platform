package ru.kcheranev.trading.core.port.income.strategyconfiguration

interface CreateStrategyConfigurationUseCase {

    fun createStrategyConfiguration(command: CreateStrategyConfigurationCommand)

}