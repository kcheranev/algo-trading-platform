package ru.kcheranev.trading.core.port.income.strategy

interface GetStrategyTypesUseCase {

    fun getStrategyTypes(): List<String>

}