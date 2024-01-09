package ru.kcheranev.trading.core.strategy

import org.springframework.stereotype.Component

@Component
class StrategyFactoryProvider(private val strategyFactories: List<StrategyFactory>) {

    fun getStrategyFactory(strategyType: String): StrategyFactory {
        return strategyFactories.first { it.strategyType() == strategyType }
    }

}