package ru.kcheranev.trading.core.port.common.model.sort

data class Sort<T : SortField>(
    val field: T,
    val order: SortDirection = SortDirection.ASC
)