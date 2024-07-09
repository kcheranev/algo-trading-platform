package ru.kcheranev.trading.core.port.model.sort

data class Sort<T : SortField>(
    val field: T,
    val order: SortDirection = SortDirection.ASC
)