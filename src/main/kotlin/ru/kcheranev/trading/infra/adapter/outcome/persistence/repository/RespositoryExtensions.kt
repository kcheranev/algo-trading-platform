package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository

import ru.kcheranev.trading.core.port.common.model.ComparedField
import ru.kcheranev.trading.core.port.common.model.Comparsion

fun StringBuilder.addAndCondition(condition: String) {
    if (isNotEmpty()) {
        append(" AND ")
    }
    append(condition)
}

fun StringBuilder.addComparsionCondition(field: String, condition: ComparedField<out Any>) {
    if (isNotEmpty()) {
        append(" AND ")
    }
    val value = condition.value
    val resultCondition =
        when (condition.comparsion) {
            Comparsion.GT -> "$field > $value"
            Comparsion.LT -> "$field < $value"
            Comparsion.EQ -> "$field = $value"
            Comparsion.GT_EQ -> "$field >= $value"
            Comparsion.LT_EQ -> "$field <= $value"
        }
    append(resultCondition)
}