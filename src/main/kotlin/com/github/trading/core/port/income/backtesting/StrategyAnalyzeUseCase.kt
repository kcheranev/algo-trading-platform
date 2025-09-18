package com.github.trading.core.port.income.backtesting

import com.github.trading.domain.model.backtesting.StrategyParametersAnalyzeResult

interface StrategyAnalyzeUseCase {

    fun analyzeStrategyOnBrokerData(command: StrategyAnalyzeOnBrokerDataCommand): List<StrategyParametersAnalyzeResult>

    fun analyzeStrategyOnStoredData(command: StrategyAnalyzeOnStoredDataCommand): List<StrategyParametersAnalyzeResult>

}