package com.github.trading.core.service

import com.github.trading.core.port.income.strategy.GetStrategyParametersNamesCommand
import com.github.trading.core.port.income.strategy.GetStrategyParametersNamesUseCase
import com.github.trading.core.port.income.strategy.GetStrategyTypesUseCase
import com.github.trading.core.strategy.factory.StrategyFactoryProvider
import org.springframework.stereotype.Service

@Service
class StrategyInfoService(
    private val strategyFactoryProvider: StrategyFactoryProvider
) : GetStrategyTypesUseCase,
    GetStrategyParametersNamesUseCase {

    override fun getStrategyTypes() = strategyFactoryProvider.getStrategyTypes()

    override fun getStrategyParametersNames(command: GetStrategyParametersNamesCommand) =
        strategyFactoryProvider.getStrategyFactory(command.strategyType)
            .strategyParameterNames()

}