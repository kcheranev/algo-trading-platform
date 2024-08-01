package ru.kcheranev.trading.core.service

import org.springframework.stereotype.Service
import ru.kcheranev.trading.core.port.income.strategy.GetStrategyParametersNamesCommand
import ru.kcheranev.trading.core.port.income.strategy.GetStrategyParametersNamesUsesCase
import ru.kcheranev.trading.core.port.income.strategy.GetStrategyTypesUseCase
import ru.kcheranev.trading.core.strategy.factory.StrategyFactoryProvider

@Service
class StrategyInfoService(
    private val strategyFactoryProvider: StrategyFactoryProvider
) : GetStrategyTypesUseCase,
    GetStrategyParametersNamesUsesCase {

    override fun getStrategyTypes() = strategyFactoryProvider.getStrategyTypes()

    override fun getStrategyParametersNames(command: GetStrategyParametersNamesCommand) =
        strategyFactoryProvider.getStrategyFactory(command.strategyType)
            .strategyParameterNames()

}