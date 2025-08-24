package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom.query

import ru.kcheranev.trading.core.port.model.Comparsion

sealed interface Condition {

    fun evaluate(): String

}

class EqualsCondition(
    private val fieldName: String
) : Condition {

    override fun evaluate() = "$fieldName = ?"

}

class ComparstionCondition(
    private val fieldName: String,
    private val comparsion: Comparsion
) : Condition {

    override fun evaluate(): String {
        return when (comparsion) {
            Comparsion.GT -> "$fieldName > ?"
            Comparsion.LT -> "$fieldName < ?"
            Comparsion.EQ -> "$fieldName = ?"
            Comparsion.GT_EQ -> "$fieldName >= ?"
            Comparsion.LT_EQ -> "$fieldName <= ?"
        }
    }

}

fun StringBuilder.addAndCondition(condition: Condition) {
    if (isNotEmpty()) {
        append(" AND ")
    }
    append(condition.evaluate())
}