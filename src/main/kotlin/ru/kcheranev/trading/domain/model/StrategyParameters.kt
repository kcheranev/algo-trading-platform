package ru.kcheranev.trading.domain.model

class StrategyParameters(map: Map<String, Any>) : HashMap<String, Any>(map) {

    fun getAsString(key: String) = this[key] as String

    fun getAsInt(key: String) = this[key] as Int

    fun getAsBoolean(key: String) = this[key] as Boolean

}