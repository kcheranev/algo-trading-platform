package ru.kcheranev.trading.core.port.income.search.model

import ru.kcheranev.trading.domain.entity.SortField

data class Sort<T : SortField>(
    val field: SortField,
    val order: T
)