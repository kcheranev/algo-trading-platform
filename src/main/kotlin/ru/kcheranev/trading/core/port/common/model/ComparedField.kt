package ru.kcheranev.trading.core.port.common.model

data class ComparedField<T>(
    val value: T,
    val comparsion: Comparsion
)