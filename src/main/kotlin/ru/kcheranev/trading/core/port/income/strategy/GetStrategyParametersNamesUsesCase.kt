package ru.kcheranev.trading.core.port.income.strategy

interface GetStrategyParametersNamesUsesCase {

    fun getStrategyParametersNames(command: GetStrategyParametersNamesCommand): List<String>

}