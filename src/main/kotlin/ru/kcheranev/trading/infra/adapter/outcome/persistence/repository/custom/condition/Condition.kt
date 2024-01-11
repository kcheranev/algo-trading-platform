package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom.condition

import java.time.LocalDate
import java.time.LocalDateTime

abstract class Condition {

    private val needWrapQuotesClasses =
        listOf(String::class, LocalDate::class, LocalDateTime::class, Enum::class)

    abstract fun evaluate(): String

    protected fun maybeWrapQuotes(value: Any): String =
        if (needWrapQuotesClasses.any { it.isInstance(value) }) {
            "'$value'"
        } else {
            value.toString()
        }

}