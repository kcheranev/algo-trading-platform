package ru.kcheranev.trading.core.port.income.backtesting

import ru.kcheranev.trading.domain.model.backtesting.StrategyAdjustAndAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.StrategyAnalyzeResult

interface StrategyAnalyzeUseCase {

    fun analyzeStrategy(command: StrategyAnalyzeCommand): StrategyAnalyzeResult

    fun adjustAndAnalyzeStrategy(command: StrategyAdjustAndAnalyzeCommand): List<StrategyAdjustAndAnalyzeResult>

}