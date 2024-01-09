package ru.kcheranev.trading.infra.adapter.outcome.persistence.repository

import ru.kcheranev.trading.infra.adapter.outcome.persistence.repository.custom.condition.Condition

fun StringBuilder.addAndCondition(condition: Condition) {
    if (isNotEmpty()) {
        append(" AND ")
    }
    append(condition.evaluate())
}