package ru.kcheranev.trading.core.strategy.factory

import org.springframework.stereotype.Component

@Component
class StrategyFactoryProvider(private val strategyFactories: List<StrategyFactory>) {

    fun getStrategyFactory(strategyType: String) = strategyFactories.first { it.strategyType() == strategyType }

    fun getStrategyTypes() = strategyFactories.map { it.strategyType() }

}