package ru.kcheranev.trading.core.strategy

import org.springframework.stereotype.Component
import ru.kcheranev.trading.domain.model.StrategyType

@Component
class StrategyFactoryProvider(private val strategyFactories: List<StrategyFactory>) {

    fun getStrategyFactory(strategyType: StrategyType): StrategyFactory {
        return strategyFactories.first { it.strategyType() == strategyType }
    }

}