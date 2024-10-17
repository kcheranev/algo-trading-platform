package ru.kcheranev.trading.core.port.income.backtesting

import ru.kcheranev.trading.domain.model.backtesting.StrategyAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.StrategyParametersAnalyzeResult

interface StrategyAnalyzeUseCase {

    fun analyzeStrategy(command: StrategyAnalyzeCommand): StrategyAnalyzeResult

    fun analyzeStrategyParameters(command: StrategyParametersAnalyzeCommand): List<StrategyParametersAnalyzeResult>

}