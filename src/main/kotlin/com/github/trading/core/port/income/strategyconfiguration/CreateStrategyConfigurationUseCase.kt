package com.github.trading.core.port.income.strategyconfiguration

interface CreateStrategyConfigurationUseCase {

    fun createStrategyConfiguration(command: CreateStrategyConfigurationCommand)

}