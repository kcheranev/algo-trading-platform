package ru.kcheranev.trading.domain.model

import ru.kcheranev.trading.domain.DomainException

class StrategyParameters(map: Map<String, Int>) : HashMap<String, Int>(map) {

    fun getParam(key: String) = get(key) ?: throw DomainException("Параметр $key торговой стратегии не определен")

}