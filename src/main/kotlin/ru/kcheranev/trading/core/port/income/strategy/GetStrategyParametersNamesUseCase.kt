package ru.kcheranev.trading.core.port.income.strategy

interface GetStrategyParametersNamesUseCase {

    fun getStrategyParametersNames(command: GetStrategyParametersNamesCommand): List<String>

}