package com.github.trading.domain.model.backtesting

import com.github.trading.domain.exception.BusinessException
import java.math.BigDecimal
import java.math.RoundingMode

data class MutableParameter(
    val value: Number,
    val direction: MutationDirection
) {

    fun getParameterVariants(divisionFactor: BigDecimal, variantsCount: Int): List<Number> {
        return when (value) {
            is Int -> {
                val minParameterValue =
                    when (direction) {
                        MutationDirection.UP -> value
                        MutationDirection.BOTH,
                        MutationDirection.DOWN ->
                            BigDecimal(value)
                                .divide(divisionFactor, BACKTESTING_RESULT_SCALE, RoundingMode.HALF_UP)
                                .toInt()
                    }
                val maxParameterValue =
                    when (direction) {
                        MutationDirection.DOWN -> value
                        MutationDirection.UP,
                        MutationDirection.BOTH -> BigDecimal(value).multiply(divisionFactor).toInt()
                    }
                val parameterValueStep =
                    ((maxParameterValue - minParameterValue) / (variantsCount - 1))
                        .let { if (it == 0) 1 else it }
                val parameterVariants = mutableListOf(value)
                for (parameterVariant in minParameterValue..maxParameterValue step parameterValueStep) {
                    parameterVariants.add(parameterVariant)
                }
                parameterVariants.distinct()
            }

            is BigDecimal -> {
                val minParameterValue =
                    when (direction) {
                        MutationDirection.UP -> value
                        MutationDirection.BOTH,
                        MutationDirection.DOWN -> value.divide(divisionFactor, BACKTESTING_RESULT_SCALE, RoundingMode.HALF_UP)
                    }
                val maxParameterValue =
                    when (direction) {
                        MutationDirection.DOWN -> value
                        MutationDirection.BOTH,
                        MutationDirection.UP -> value.multiply(divisionFactor)
                    }
                val paramValueStep =
                    (maxParameterValue - minParameterValue)
                        .divide(BigDecimal(variantsCount - 1), BACKTESTING_RESULT_SCALE, RoundingMode.HALF_UP)
                val parameterVariants = mutableListOf(value)
                var parameterVariant = minParameterValue
                while (parameterVariant <= maxParameterValue) {
                    parameterVariants.add(parameterVariant)
                    parameterVariant += paramValueStep
                }
                parameterVariants.distinct()
            }

            else -> throw BusinessException("Unexpected parameter $value value type")
        }
    }
}

enum class MutationDirection {

    UP, DOWN, BOTH

}