package ru.kcheranev.trading.core.port.income.search.model

data class ComparedField<T>(
    val field: T,
    val comparsion: Comparsion
)