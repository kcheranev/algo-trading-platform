package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom.condition

import java.time.LocalDate
import java.time.LocalDateTime

sealed class Condition {

    private val needWrapQuotesClasses =
        listOf(String::class, LocalDate::class, LocalDateTime::class)

    abstract fun evaluate(): String

    protected fun needWrapQuotes(value: Any): Boolean =
        needWrapQuotesClasses.any { it.isInstance(value) }

}