package ru.kcheranev.trading.core.port.income.backtesting

import ru.kcheranev.trading.domain.model.backtesting.StrategyAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.StrategyParametersAnalyzeResult

interface StrategyAnalyzeUseCase {

    fun analyzeStrategyOnBrokerData(command: StrategyAnalyzeOnBrokerDataCommand): StrategyAnalyzeResult

    fun analyzeStrategyOnStoredData(command: StrategyAnalyzeOnStoredDataCommand): StrategyAnalyzeResult

    fun analyzeStrategyParametersOnBrokerData(command: StrategyParametersAnalyzeOnBrokerDataCommand): List<StrategyParametersAnalyzeResult>

    fun analyzeStrategyParametersOnStoredData(command: StrategyParametersAnalyzeOnStoredDataCommand): List<StrategyParametersAnalyzeResult>

}