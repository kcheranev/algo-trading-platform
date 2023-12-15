package ru.kcheranev.trading.infra.adapter.outcome.persistence.model

data class MapWrapper<K, V>(
    val value: Map<K, V>
)