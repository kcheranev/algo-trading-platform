package ru.kcheranev.trading.domain.model

import ru.kcheranev.trading.domain.DomainException
import java.math.BigDecimal

class StrategyParameters(map: Map<String, Number>) : HashMap<String, Number>(map) {

    fun getAsInt(key: String) =
        get(key) as Int? ?: throw DomainException("Trade strategy parameter $key is not found")

    fun getAsBigDecimal(key: String) =
        get(key) as BigDecimal? ?: throw DomainException("Trade strategy parameter $key is not found")

}