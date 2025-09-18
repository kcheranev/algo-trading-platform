package com.github.trading.core.port.model.sort

data class Sort<T : SortField>(
    val field: T,
    val order: SortDirection = SortDirection.ASC
)