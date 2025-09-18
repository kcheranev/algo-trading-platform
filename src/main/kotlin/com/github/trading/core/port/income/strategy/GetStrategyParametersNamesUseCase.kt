package com.github.trading.core.port.income.strategy

interface GetStrategyParametersNamesUseCase {

    fun getStrategyParametersNames(command: GetStrategyParametersNamesCommand): List<String>

}