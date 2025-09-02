package ru.kcheranev.trading.core.port.income.backtesting

import ru.kcheranev.trading.domain.model.backtesting.StrategyParametersAnalyzeResult

interface StrategyAnalyzeUseCase {

    fun analyzeStrategyOnBrokerData(command: StrategyAnalyzeOnBrokerDataCommand): List<StrategyParametersAnalyzeResult>

    fun analyzeStrategyOnStoredData(command: StrategyAnalyzeOnStoredDataCommand): List<StrategyParametersAnalyzeResult>

}