package ru.kcheranev.trading.domain.model

abstract class StrategyParameters : HashMap<String, Any>() {

    fun getAsString(key: String) = this[key] as String

    fun getAsInt(key: String) = this[key] as Int

    fun getAsBoolean(key: String) = this[key] as Boolean

}