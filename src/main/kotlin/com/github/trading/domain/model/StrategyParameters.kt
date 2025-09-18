package com.github.trading.domain.model

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.github.trading.core.error.ValidationError
import com.github.trading.domain.exception.ValidationException
import java.math.BigDecimal

class StrategyParameters(map: Map<String, Number>) : HashMap<String, Number>(map) {

    fun getAsInt(key: String): Either<ValidationError, Int> =
        either {
            ensureNotNull(get(key)?.toInt()) { ValidationError(listOf("Trade strategy parameter $key is not present")) }
        }

    fun getAsIntOrThrow(key: String) =
        get(key)?.toInt() ?: throw ValidationException("Trade strategy parameter $key is not present")

    fun getAsIntOrThrow(strategyParameter: StrategyParameter) = getAsIntOrThrow(strategyParameter.alias())

    fun getAsBigDecimal(key: String): Either<ValidationError, BigDecimal> =
        either {
            val parameterValue = get(key)?.let { parameterValue -> BigDecimal(parameterValue.toString()) }
            ensureNotNull(parameterValue) { ValidationError(listOf("Trade strategy parameter $key is not present")) }
        }

    fun getAsBigDecimalOrThrow(key: String) =
        get(key)?.let { parameterValue -> BigDecimal(parameterValue.toString()) }
            ?: throw ValidationException("Trade strategy parameter $key is not present")

    fun getAsBigDecimalOrThrow(strategyParameter: StrategyParameter) = getAsBigDecimalOrThrow(strategyParameter.alias())

}

interface StrategyParameter {

    fun alias(): String

}