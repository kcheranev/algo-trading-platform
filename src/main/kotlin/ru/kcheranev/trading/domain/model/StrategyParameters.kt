package ru.kcheranev.trading.domain.model

import ru.kcheranev.trading.core.exception.StrategyParamValidationException
import java.math.BigDecimal

class StrategyParameters(map: Map<String, Number>) : HashMap<String, Number>(map) {

    fun getAsInt(key: String) =
        get(key) as Int? ?: throw StrategyParamValidationException("Trade strategy parameter $key is not present")

    fun getAsInt(strategyParameter: StrategyParameter) = getAsInt(strategyParameter.alias())

    fun getAsBigDecimal(key: String) =
        get(key) as BigDecimal?
            ?: throw StrategyParamValidationException("Trade strategy parameter $key is not present")

    fun getAsBigDecimal(strategyParameter: StrategyParameter) = getAsBigDecimal(strategyParameter.alias())

}

interface StrategyParameter {

    fun alias(): String

}