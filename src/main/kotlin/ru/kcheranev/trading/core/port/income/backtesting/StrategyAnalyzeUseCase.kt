package ru.kcheranev.trading.core.port.income.backtesting

import ru.kcheranev.trading.domain.model.backtesting.StrategyAnalyzeResult

interface StrategyAnalyzeUseCase {

    fun analyzeStrategy(command: StrategyAnalyzeCommand): StrategyAnalyzeResult

}