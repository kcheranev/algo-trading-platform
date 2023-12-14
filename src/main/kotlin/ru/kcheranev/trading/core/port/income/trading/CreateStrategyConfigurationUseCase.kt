package ru.kcheranev.trading.core.port.income.trading

interface CreateStrategyConfigurationUseCase {

    fun createStrategyConfiguration(command: CreateStrategyConfigurationCommand)

}