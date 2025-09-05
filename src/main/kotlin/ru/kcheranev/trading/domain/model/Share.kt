package ru.kcheranev.trading.domain.model

data class Share(
    val id: String,
    val ticker: String,
    val lot: Int
)