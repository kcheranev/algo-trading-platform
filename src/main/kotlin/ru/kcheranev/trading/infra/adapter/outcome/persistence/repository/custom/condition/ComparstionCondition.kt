package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom.condition

import ru.kcheranev.trading.core.port.common.model.ComparedField
import ru.kcheranev.trading.core.port.common.model.Comparsion

class ComparstionCondition(
    private val fieldName: String,
    private val comparedField: ComparedField<out Any>
) : Condition() {

    override fun evaluate(): String {
        val comparsionValue = maybeWrapQuotes(comparedField.value)
        return when (comparedField.comparsion) {
            Comparsion.GT -> "$fieldName > $comparsionValue"
            Comparsion.LT -> "$fieldName < $comparsionValue"
            Comparsion.EQ -> "$fieldName = $comparsionValue"
            Comparsion.GT_EQ -> "$fieldName >= $comparsionValue"
            Comparsion.LT_EQ -> "$fieldName <= $comparsionValue"
        }
    }

}