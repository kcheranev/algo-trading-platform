package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom.condition

class EqualsCondition(
    private val fieldName: String,
    private val comparsionValue: Any
) : Condition() {

    override fun evaluate() =
        if (needWrapQuotes(comparsionValue)) {
            "$fieldName = '$comparsionValue'"
        } else {
            "$fieldName = $comparsionValue"
        }

}