package ru.kcheranev.trading.core.port.model

data class ComparedField<T>(
    val value: T,
    val comparsion: Comparsion
)