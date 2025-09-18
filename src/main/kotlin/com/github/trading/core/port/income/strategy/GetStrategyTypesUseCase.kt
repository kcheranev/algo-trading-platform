package com.github.trading.core.port.income.strategy

interface GetStrategyTypesUseCase {

    fun getStrategyTypes(): List<String>

}