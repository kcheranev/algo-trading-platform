package ru.kcheranev.trading.infra.adapter.outcome.persistence.model

data class MapWrapper<K>(
    val value: Map<K, Number>
)