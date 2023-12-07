package ru.kcheranev.trading.core.port.common.model

import ru.kcheranev.trading.domain.entity.SortField

data class Sort<T : SortField>(
    val field: T,
    val order: SortDirection = SortDirection.ASC
)