package ru.kcheranev.trading.core.port.income.backtesting

import ru.kcheranev.trading.domain.model.backtesting.PeriodStrategyAnalyzeResult
import ru.kcheranev.trading.domain.model.backtesting.StrategyAdjustAndAnalyzeResult

interface StrategyAnalyzeUseCase {

    fun analyzeStrategy(command: StrategyAnalyzeCommand): PeriodStrategyAnalyzeResult

    fun adjustAndAnalyzeStrategy(command: StrategyAdjustAndAnalyzeCommand): List<StrategyAdjustAndAnalyzeResult>

}