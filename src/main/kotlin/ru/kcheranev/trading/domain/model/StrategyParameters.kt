package ru.kcheranev.trading.domain.model

import ru.kcheranev.trading.domain.DomainException
import java.math.BigDecimal

class StrategyParameters(map: Map<String, Number>) : HashMap<String, Number>(map) {

    fun getAsInt(key: String) =
        get(key) as Int? ?: throw DomainException("Trade strategy parameter $key is not found")

    fun getAsInt(strategyParameter: StrategyParameter) = getAsInt(strategyParameter.alias())

    fun getAsBigDecimal(key: String) =
        get(key) as BigDecimal? ?: throw DomainException("Trade strategy parameter $key is not found")

    fun getAsBigDecimal(strategyParameter: StrategyParameter) = getAsBigDecimal(strategyParameter.alias())

}

interface StrategyParameter {

    fun alias(): String

}